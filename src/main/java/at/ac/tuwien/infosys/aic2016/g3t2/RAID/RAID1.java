package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.AWS;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Blob;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RAID1 implements IRAID {

    private final List<IBlobstore> blobstores;

    public RAID1(List<IBlobstore> blobstores) {
        this.blobstores = blobstores;
    }

    public RAID1() {
        this.blobstores = new ArrayList<>();
        this.blobstores.add(new AWS());
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
        for (IBlobstore bs : this.blobstores) {
            boolean result = bs.delete(storagefilename);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    @Override
    public File read(String storagefilename) throws ItemMissingException {
        ArrayList<Location> locations = new ArrayList<>();
        byte[] data = null;

        for (IBlobstore bs : this.blobstores) {
            try {
                Blob blob = bs.read(storagefilename);
                data = blob.getData();
            } catch (ItemMissingException e) {
                // TODO recover the broken copy?
            }
        }

        if (data == null) {
            throw new ItemMissingException();
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
