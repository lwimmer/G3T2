package at.ac.tuwien.infosys.aic2016.g3t2.version;

import java.util.List;

import at.ac.tuwien.infosys.aic2016.g3t2.RAID.File;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.IRAID;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.RAIDType;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;

public interface IVersionManager extends IRAID {

	/**
     * Create a new file.
     *
     * @param filename name of the file
     * @param data content of the file
     * @param raidType storage type to create file
     * @return true if successfully created
     */
	boolean create(String filename, byte[] data, RAIDType raidType);

	/**
     * Update a file.
     *
     * @param filename name of the file
     * @param data content of the file
     * @return true if successfully created
     */
	boolean update(String filename, byte[] data) throws ItemMissingException, UserinteractionRequiredException;

	/**
     * Read a file.
     *
     * @param filename name of the file
     * @param version version of the file
     * @return the file as File
     */
	File read(String filename, int version) throws ItemMissingException, UserinteractionRequiredException;

	/**
     * Return file versions
     *
     * @param filename name of the file
     * @return list of versions as Integer
     */
	List<Integer> getFileVersions(String filename) throws ItemMissingException;

	/**
     * Return the last version of the file.
     *
     * @param filename name of the file
     * @return the last version of the file as int
     */
	int getLastVersion(String filename) throws ItemMissingException;

}
