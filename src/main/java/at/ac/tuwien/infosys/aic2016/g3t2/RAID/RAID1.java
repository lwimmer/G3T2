package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Blob;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;

@Service
public class RAID1 extends AbstractRAID {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static int MINIMUM_BLOBSTORES = 1;
    protected final static RAIDType RAID_TYPE = RAIDType.RAID1;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    
    public RAID1(Collection<IBlobstore> blobstores) {
        super(MINIMUM_BLOBSTORES, RAID_TYPE, blobstores);
    }

    public RAID1(IBlobstore... blobstoresArray) {
        super(MINIMUM_BLOBSTORES, RAID_TYPE, blobstoresArray);
    }

    @Autowired
    public RAID1(Map<String, IBlobstore> blobstoresMap, 
            @Value("#{'${disabled_blobstores:}'.split(',')}") List<String> disabledBlobstores) {
        super(MINIMUM_BLOBSTORES, RAID_TYPE, blobstoresMap, disabledBlobstores);
    }
    
    @Override
    public boolean create(String storagefilename, byte[] data) {
        boolean success = false;

        List<Future<Boolean>> futures = new ArrayList<>();
        for (IBlobstore bs : this.blobstores) {
            Callable<Boolean> worker = () -> {
                logger.info("Uploading file {} to {}", storagefilename, bs.getClass().getSimpleName());
                Boolean ret = bs.create(addPrefix(storagefilename), data);
                logger.info("Finished uploading to {}", bs.getClass().getSimpleName());
                return ret;
            };
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
        List<Future<Boolean>> futures = new ArrayList<>();
        boolean deleted = false;

        for (IBlobstore bs : this.blobstores) {
            Callable<Boolean> worker = () -> bs.delete(addPrefix(storagefilename));
            futures.add(pool.submit(worker));
        }

        for (Future<Boolean> f : futures) {
            try {
                if (f.get()) {
                    deleted = true;
                } else {
                    return false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof ItemMissingException) {
                    // ignore
                } else {
                    logger.error("Unhandleded exception. throwing up");
                    throw new RuntimeException(e);
                }
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

        HashMap<IBlobstore, Future<Blob>> futures = new HashMap<>();

        for (IBlobstore bs : this.blobstores) {
            Callable<Blob> worker = () -> {
                logger.info("Fetching file {} from {}", storagefilename, bs.getClass().getSimpleName());
                Blob blob = null;
                try {
                    blob = bs.read(addPrefix(storagefilename));
                } catch (ItemMissingException e) {
                }
                logger.info("Finished fetching from {}", bs.getClass().getSimpleName());
                return blob;
            };
            futures.put(bs, pool.submit(worker));
        }

        for (Map.Entry<IBlobstore, Future<Blob>> entry : futures.entrySet()) {
            IBlobstore bs = entry.getKey();
            Future<Blob> f = entry.getValue();

            try {
                Blob blob = f.get();
                if (blob != null) {
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
                    locations.put(bs, new Location(bs.getClass().getSimpleName(), addPrefix(storagefilename), true, false));
                } else {
                    bad.add(bs);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
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
            boolean success = bs.create(addPrefix(storagefilename), goodData);
            if (!success) {
                logger.error("Failed to restore inconsistent replica");
            } else {
                locations.put(bs, new Location(replicaName, addPrefix(storagefilename), true, true));
                logger.info("Recovery of replica {}, file '{}' complete.", replicaName, storagefilename);
            }
        }

        List<Location> locationList = new ArrayList<>();
        for (IBlobstore bs: this.blobstores) {
            locationList.add(locations.get(bs));
        }

        return new File(goodData, new FileMetadata(RAIDType.RAID1, locationList));
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
}
