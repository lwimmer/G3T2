package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * A file describes one file stored on the storage providers by the RAID.
 */
public class File {
    /**
     * Return the content of the file.
     * @return content of the file
     */
    public byte[] getData() {
        throw new NotImplementedException();
    }

    /**
     * Return a list of locations that describe where the file is saved.
     * @return list of locations
     */
    public List<Location> getLocations() {
        throw new NotImplementedException();
    }
}
