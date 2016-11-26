package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

public class Location {

    private String blobstore;
    private String filename;
    private boolean original;

    public Location (String blobstore, String filename, boolean original) {
        this.blobstore = blobstore;
        this.filename = filename;
        this.original = original;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isOriginal() {
        return original;
    }

    public String getBlobstore() {
        return blobstore;
    }
}
