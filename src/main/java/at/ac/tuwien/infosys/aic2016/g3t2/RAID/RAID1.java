package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Blob;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;

@Service
@Lazy
public class RAID1 implements IRAID {

    private final Collection<IBlobstore> blobstores;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RAID1(IBlobstore... blobstoresArray) {
        blobstores = Arrays.asList(blobstoresArray);
    }

    @Autowired
    public RAID1(Map<String, IBlobstore> blobstoresMap,
            @Value("#{'${disabled_blobstores:}'.split(',')}") List<String> disabledBlobstores) {
        if (disabledBlobstores != null)
            for (String disabled : disabledBlobstores)
                blobstoresMap.remove(disabled);
        blobstores = blobstoresMap.values();
    }

    public RAID1(Collection<IBlobstore> blobstores) {
        this.blobstores = blobstores;
    }

    @Override
    public boolean create(String storagefilename, byte[] data) {
        boolean success = false;
        for (IBlobstore bs : this.blobstores) {
            boolean result = bs.create(storagefilename, data);
            if (result) {
                success = true;
            }
        }
        return success;
    }

    @Override
    public boolean delete(String storagefilename) throws ItemMissingException {
        boolean deleted = false;

        for (IBlobstore bs : this.blobstores) {

            try {
                boolean result = bs.delete(storagefilename);
                if (result) {
                    deleted = true;
                } else {
                    return false;
                }
            } catch (ItemMissingException e) {
            }
        }

        if (!deleted) {
            throw new ItemMissingException();
        }

        return true;
    }

    @Override
    public File read(String storagefilename) throws ItemMissingException {
        ArrayList<Location> locations = new ArrayList<>();
        ArrayList<IBlobstore> bad = new ArrayList<>();
        ArrayList<String> hashes = new ArrayList<>();
        String goodHash = null;
        byte[] data = null;

        for (IBlobstore bs : this.blobstores) {
            try {
                Blob blob = bs.read(storagefilename);
                String hash = DigestUtils.sha1Hex(blob.getData());
                // TODO store the hash somewhere instead of assuming that the
                // first file is good
                if (goodHash == null) {
                    goodHash = hash;
                }
                hashes.add(hash);
                if (hash.equals(goodHash)) {
                    if (data == null) {
                        data = blob.getData();
                    }
                } else {
                    bad.add(bs);
                }

                locations.add(new Location(bs.getClass().getSimpleName(), storagefilename, true, false));
            } catch (ItemMissingException e) {
                bad.add(bs);
            }
        }

        if (data == null) {
            throw new ItemMissingException();
        }

        // recover inconsistent replicas
        for (IBlobstore bs : bad) {
            String replicaName = bs.getClass().getSimpleName();
            logger.info("Detected inconsistent replica {}, file '{}'. Recovering...", replicaName, storagefilename);
            bs.create(storagefilename, data);
            locations.add(new Location(replicaName, storagefilename, true, true));
            logger.info("Recovery of replica {}, file '{}' complete.", replicaName, storagefilename);
        }

        return new File(data, locations);
    }

    @Override
    public List<String> listFiles() {
        Set<String> result = new LinkedHashSet<>();
        for (IBlobstore bs : this.blobstores) {
            result.addAll(bs.listBlobs());
        }
        return new ArrayList<>(result);
    }
}
