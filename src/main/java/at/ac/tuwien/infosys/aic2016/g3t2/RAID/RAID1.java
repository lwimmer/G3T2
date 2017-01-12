package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.*;
import java.util.concurrent.*;

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;
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
    private final ExecutorService pool;

    public RAID1(IBlobstore... blobstoresArray) {
        blobstores = Arrays.asList(blobstoresArray);
        pool = Executors.newCachedThreadPool();
    }

    @Autowired
    public RAID1(Map<String, IBlobstore> blobstoresMap,
            @Value("#{'${disabled_blobstores:}'.split(',')}") List<String> disabledBlobstores) {
        if (disabledBlobstores != null)
            for (String disabled : disabledBlobstores)
                blobstoresMap.remove(disabled);
        blobstores = blobstoresMap.values();
        pool = Executors.newCachedThreadPool();
    }

    public RAID1(Collection<IBlobstore> blobstores) {
        this.blobstores = blobstores;
        pool = Executors.newCachedThreadPool();
    }

    @Override
    public boolean create(String storagefilename, byte[] data) {
        boolean success = false;

        List<Future<Boolean>> futures = new ArrayList<>();
        for (IBlobstore bs : this.blobstores) {
            Callable<Boolean> worker = () -> bs.create(storagefilename, data);
            futures.add(pool.submit(worker));
        }

        for (Future<Boolean> f : futures) {
            boolean result = false;
            try {
                result = f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
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
    public File read(String storagefilename) throws ItemMissingException, UserinteractionRequiredException {
        HashMap<IBlobstore, Location> locations = new HashMap<>();
        Set<IBlobstore> bad = new HashSet<>();
        Set<IBlobstore> good = new HashSet<>();
        HashMap<String, ArrayList<IBlobstore>> hashes = new HashMap<>();
        HashMap<IBlobstore, byte[]> dataItems = new HashMap<>();

        for (IBlobstore bs : this.blobstores) {
            try {
                Blob blob = bs.read(storagefilename);
                String hash = DigestUtils.sha1Hex(blob.getData());
                ArrayList<IBlobstore> list = hashes.get(hash);
                if (list == null) {
                    list = new ArrayList<>();
                    list.add(bs);
                    hashes.put(hash, list);
                } else {
                    list.add(bs);
                }

                dataItems.put(bs, blob.getData());
                locations.put(bs, new Location(bs.getClass().getSimpleName(), storagefilename, true, false));
            } catch (ItemMissingException e) {
                bad.add(bs);
            }
        }

        if (dataItems.size() == 0) {
            throw new ItemMissingException();
        }

        if (dataItems.size() == 1) {
            // file only exists only once -> probably failed deletion -> delete
            // TODO implement deletion, add test
        }

        categorizeReplicaByHash(hashes, bad, good);

        if (good.size() < blobstores.size()/2) {
            throw new UserinteractionRequiredException("Less than half the number of blobstores agree on the content of '"+storagefilename+"'. Please manually fix the incorrect ones.");
        }

        IBlobstore goodStore = good.iterator().next();
        byte[] goodData = dataItems.get(goodStore);

        // recover inconsistent replicas
        for (IBlobstore bs : bad) {
            String replicaName = bs.getClass().getSimpleName();
            logger.info("Detected inconsistent replica {}, file '{}'. Recovering...", replicaName, storagefilename);
            boolean success = bs.create(storagefilename, goodData);
            if (!success) {
                logger.error("Failed to restore inconsistent replica");
            } else {
                locations.put(bs, new Location(replicaName, storagefilename, true, true));
                logger.info("Recovery of replica {}, file '{}' complete.", replicaName, storagefilename);
            }
        }

        List<Location> locationList = new ArrayList<>();
        for (IBlobstore bs: this.blobstores) {
            locationList.add(locations.get(bs));
        }

        return new File(goodData, locationList);
    }

    private void categorizeReplicaByHash(HashMap<String, ArrayList<IBlobstore>> hashes, Set<IBlobstore> bad, Set<IBlobstore> good) throws UserinteractionRequiredException {
        String bestHash = null;
        int bestCount = 0;

        for (Map.Entry<String, ArrayList<IBlobstore>> entry : hashes.entrySet()) {
            int count = entry.getValue().size();
            if (bestHash == null || count > bestCount) {
                bestHash = entry.getKey();
                bestCount = count;
            }
        }

        if (bestCount == 1 && hashes.size() > 1) {
            throw new UserinteractionRequiredException("All replicas differ. Please manually delete the incorrect files.");
        }


        for (Map.Entry<String, ArrayList<IBlobstore>> entry : hashes.entrySet()) {
            if (entry.getKey().equals(bestHash)) {
                good.addAll(entry.getValue());
            } else {
                bad.addAll(entry.getValue());
            }
        }
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
