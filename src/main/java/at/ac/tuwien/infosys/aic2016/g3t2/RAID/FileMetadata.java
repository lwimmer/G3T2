package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.List;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;

public class FileMetadata {
    protected final RAIDType raidType;
    protected final List<Location> locations;
    
    public FileMetadata() {
        raidType = null;
        locations = null;
    }
    
    public FileMetadata(RAIDType raidType, List<Location> locations) {
        this.raidType = raidType;
        this.locations = locations;
    }
    
    /**
     * Return the raid type of the file
     * @return the raid type of the file
     */
    public RAIDType getRaidType() {
        return raidType;
    }
    
    /**
     * Return a list of locations that describe where the file is saved.
     * @return list of locations
     */
    public List<Location> getLocations() {
        return locations;
    }
}
