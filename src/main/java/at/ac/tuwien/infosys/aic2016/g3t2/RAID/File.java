package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.List;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;

/**
 * A file describes one file stored on the storage providers by the RAID.
 */
public class File {
    private final byte[] data;
    private final List<Location> locations;

    public static File removeMeta(File file) {
    	return new File(null, file.locations);
    }
    
    public File(byte[] data, List<Location> locations) {
        this.data = data;
        this.locations = locations;
    }
    
    /**
     * Return the content of the file.
     * @return content of the file
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Return a list of locations that describe where the file is saved.
     * @return list of locations
     */
    public List<Location> getLocations() {
        return locations;
    }
}
