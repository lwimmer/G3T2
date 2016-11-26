package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

public class Location {

    private String blobstore;
    private String filename;
    private boolean original;
    private boolean recovered;

    public Location(String blobstore, String filename, boolean original, boolean recovered) {
        this.blobstore = blobstore;
        this.filename = filename;
        this.original = original;
        this.recovered = recovered;
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

    public boolean isRecovered() {
        return recovered;
    }
}
