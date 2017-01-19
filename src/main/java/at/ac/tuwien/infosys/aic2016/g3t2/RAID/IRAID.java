package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;

import java.util.List;

/**
 * The RAID distributes our data across storage providers in a RAID like fashion. This means that a file only has to
 * exist on any one provider. If it is missing or corrupted on other providers the RAID class automatically corrects
 * this error.
 *
 * It allows to add, read and remove data, get a list of files and verify that the stored data is consistent.
 */
public interface IRAID {
    /**
     * Create a new file.
     *
     * @param storagefilename Name of the file
     * @param data content of the file
     * @return true if successfully created
     */
    boolean create(String storagefilename, byte[] data);

    /**
     * Delete a file.
     *
     * @param storagefilename Name of the file
     * @return true if successfully deleted
     */
    boolean delete(String storagefilename) throws ItemMissingException;

    /**
     * Return the data of a single file.
     *
     * @param storagefilename Name of the file
     * @return the file as File
     */
    File read(String storagefilename) throws ItemMissingException, UserinteractionRequiredException;

    /**
     * Return a list of file names saved in the RAID.
     * @return list of file names.
     */
    List<String> listFiles();
}
