package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;

@Service
public class RAID5 extends AbstractRAID {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static int MINIMUM_BLOBSTORES = 3;

    public RAID5(Collection<IBlobstore> blobstores) {
        super(MINIMUM_BLOBSTORES, blobstores);
    }

    public RAID5(IBlobstore... blobstoresArray) {
        super(MINIMUM_BLOBSTORES, blobstoresArray);
    }

    @Autowired
    public RAID5(Map<String, IBlobstore> blobstoresMap, 
            @Value("#{'${disabled_blobstores:}'.split(',')}") List<String> disabledBlobstores) {
        super(MINIMUM_BLOBSTORES, blobstoresMap, disabledBlobstores);
    }

    protected int numParts() {
        return blobstores.size() - 1;
    }
    
    protected static int calcPartSize(int numParts, int totalLength) {
        return (totalLength / numParts) + (totalLength % numParts == 0 ? 0 : 1);
    }
    
    protected static byte[] getPart(byte[] data, int partSize, int partNum) {
        return Arrays.copyOfRange(data, partSize * partNum, partSize * (partNum + 1));
    }
    
    protected byte[] xor(byte[][] blocks) {
        final byte[] result = blocks[0].clone();
        for (int i = 1; i < blocks.length; i++) {
            final byte[] block = blocks[i];
            for (int j = 0; j < result.length; j++)
                result[j] ^= block[j];
        }
        return result;
    }
    
    protected RAID5File readRAID5File(IBlobstore blobstore, String storagefilename) {
        try {
            final Location location = new Location(blobstore.getClass().getSimpleName(), storagefilename);
            return new RAID5File(location, blobstore.read(storagefilename).getData());
        } catch (ItemMissingException | IOException e) {
            return null;
        }
    }
    
    @Override
    public boolean create(final String storagefilename, final byte[] data) {
        final int numParts = numParts();
        final int partSize = calcPartSize(numParts, data.length);
        final long fileSize = data.length;
        
        final List<RAID5File> parts = IntStream.range(0, numParts).parallel()
            .mapToObj(i -> new RAID5File(fileSize, numParts, i, false, getPart(data, partSize, i)))
            .collect(Collectors.toList());
        
        final byte[] parity = xor(parts.stream().map(RAID5File::getData).toArray(byte[][]::new));
        final RAID5File parityFile = new RAID5File(fileSize, numParts, -1, true, parity);
        parts.add(parityFile);
        
        final AtomicInteger counter = new AtomicInteger();
        return blobstores
            .parallelStream()
            .map(bs -> bs.create(storagefilename, parts.get(counter.getAndIncrement()).encode()))
            .allMatch(result -> result);
    }

    protected boolean safeDelete(IBlobstore blobstore, String storagefilename) {
        try {
            return blobstore.delete(storagefilename);
        } catch (ItemMissingException e) {
            return false;
        }
    }
    
    @Override
    public boolean delete(String storagefilename) throws ItemMissingException {
        final int numDeleted = blobstores.parallelStream().map(bs -> safeDelete(bs, storagefilename)).mapToInt(r -> r ? 1 : 0).sum();
        if (numDeleted == 0)
            throw new ItemMissingException();
        return numDeleted == blobstores.size();
    }

    @Override
    public File read(final String storagefilename) throws ItemMissingException, UserinteractionRequiredException {
        final List<RAID5File> allParts = blobstores
            .parallelStream()
            .map(bs -> readRAID5File(bs, storagefilename))
            .filter(bs -> bs != null)
            .collect(Collectors.toList());
        
        if (allParts.size() == 0)
            throw new ItemMissingException();
        int numParts = allParts.get(0).getNumParts();
        long fileSize = allParts.get(0).getFileSize();
        if (allParts.size() < numParts)
            throw new UserinteractionRequiredException("more than one part is missing or damaged");
        
        RAID5File parity = null;
        final RAID5File[] dataParts = new RAID5File[numParts];
        for (RAID5File part : allParts) {
            if (part.isParity) {
                parity = part;
            } else {
                dataParts[part.getPartNum()] = part;
                part.setLocation(new Location(part.getLocation(), true, false));
            }
        }
        
        int missingPart = -1;
        for (int i = 0; i < numParts; i++) {
            if (dataParts[i] == null) {
                if (missingPart >= 0)
                    throw new UserinteractionRequiredException("more than one part is missing or damaged");
                missingPart = i;
            }
        }
        if (missingPart >= 0 && parity == null)
            throw new UserinteractionRequiredException("one part AND parity are missing or damaged");
        
        if (missingPart >= 0 || parity == null) { // recovery needed
            
            final byte[] missingBlock = xor(Stream.concat(Arrays.stream(dataParts), Stream.of(parity))
                    .filter(p -> p != null).map(RAID5File::getData).toArray(byte[][]::new));

            // find which blobstore is missing a part
            final Set<String> usedBlobstores = allParts.stream().map(p -> p.getLocation().getBlobstore()).collect(Collectors.toSet());
            final Optional<IBlobstore> targetBlobstore = blobstores.stream().filter(bs -> ! usedBlobstores.contains(bs.getClass().getSimpleName())).findAny();
            if (! targetBlobstore.isPresent())
                throw new UserinteractionRequiredException("could not find a free blobstore to put missing part");
            
            if (parity == null) { // need to recover parity
                parity = new RAID5File(fileSize, numParts, -1, true, missingBlock);
                parity.setLocation(new Location(targetBlobstore.get().getClass().getSimpleName(), storagefilename, false, true));
                targetBlobstore.get().create(storagefilename, parity.encode());
            } else if (missingPart >= 0) { // need to recover a part
                dataParts[missingPart] = new RAID5File(fileSize, numParts, missingPart, false, missingBlock);
                dataParts[missingPart].setLocation(new Location(targetBlobstore.get().getClass().getSimpleName(), storagefilename, true, true));
                targetBlobstore.get().create(storagefilename, dataParts[missingPart].encode());
            }
        }
        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(dataParts[0].data.length * dataParts.length);
        for (RAID5File part : dataParts) {
            try {
                baos.write(part.getData());
            } catch (IOException e) { // should never happen with ByteArrayOutputStream
            }
        }
        final List<Location> locations = Stream.concat(Arrays.stream(dataParts), Stream.of(parity)).map(RAID5File::getLocation).collect(Collectors.toList());
        return new File(Arrays.copyOf(baos.toByteArray(), (int)fileSize), locations);
    }

}
