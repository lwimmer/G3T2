package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.List;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;

/**
 * A file describes one file stored on the storage providers by the RAID.
 */
public class File {
    private final byte[] data;
    private final FileMetadata metadata;

    public File(byte[] data, FileMetadata metadata) {
        this.data = data;
        this.metadata = metadata;
    }
    
    /**
     * Return the content of the file.
     * @return content of the file
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Return the metadata of the file
     * @return file metadata
     */
    public FileMetadata getMetadata() {
        return metadata;
    }
    
    /**
     * Return a list of locations that describe where the file is saved.
     * @return list of locations
     */
    public List<Location> getLocations() {
        if (metadata == null)
            return null;
        return metadata.getLocations();
    }
    
}
