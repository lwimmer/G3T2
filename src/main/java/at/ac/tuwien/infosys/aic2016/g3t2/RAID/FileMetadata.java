package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;

public class FileMetadata {
	protected final RAIDType raidType;
	protected final List<Location> locations;
	protected List<Integer> versions;

	public FileMetadata() {
		raidType = null;
		locations = null;
		versions = null;
	}

	public FileMetadata(RAIDType raidType, List<Location> locations) {
		this.raidType = raidType;
		this.locations = locations;
		this.versions = new ArrayList<>();
	}

	public FileMetadata(RAIDType raidType, List<Location> locations, List<Integer> versions) {
		this.raidType = raidType;
		this.locations = locations;
		this.versions = versions;
	}

	/**
	 * Return the raid type of the file
	 * 
	 * @return the raid type of the file
	 */
	public RAIDType getRaidType() {
		return raidType;
	}

	/**
	 * Return a list of locations that describe where the file is saved.
	 * 
	 * @return list of locations
	 */
	public List<Location> getLocations() {
		return locations;
	}

	/**
	 * Return a list of versions of the file.
	 * 
	 * @return list of versions.
	 */
	public List<Integer> getVersions() {
		return versions;
	}
	
	/**
	 * Sets list of versions
	 * @param list of versions
	 */
	public void setVersions(List<Integer> versions) {
		this.versions = versions;
	}
}
