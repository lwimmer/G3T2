package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;

import java.util.List;

/**
 * A blobstore stores data in a key-value fashion. The key is the blobname.
 *
 * It allows to add, read and remove data as well as providing a list of its content.
 */
public interface IBlobstore {
    /**
     * Create a new item in the blobstore and store the passed data in it.
     *
     * @param blobname name of the item
     * @param data data to store
     * @return true if sucessfully stored
     */
    boolean create(String blobname, byte[] data);

    /**
     * Delete an item from the blobstore.
     *
     * @param blobname name of the item
     * @return true if sucessfully deleted
     */
    boolean delete(String blobname) throws ItemMissingException;

    /**
     * Return an item from the blobstore.
     *
     * @param blobname name of the item
     * @return the item as a Blob
     */
    Blob read(String blobname) throws ItemMissingException;

    /**
     * Return a list of item names that are saved in the blobstore.
     * @return list of item names
     */
    List<String> listBlobs();
}
