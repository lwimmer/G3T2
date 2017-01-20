package at.ac.tuwien.infosys.aic2016.g3t2.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.infosys.aic2016.g3t2.RAID.File;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.IRAID;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;

@Service
public class VersionManager implements IVersionManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String VERSION_REGEX = "_[v][0-9]{0,2}$";

	private final String RAID_REGEX = "^[r][1,5]";

	private final String VERSION_SUFFIX = "_v";

	@Autowired
	private Map<String, IRAID> raidMap;

	@PostConstruct
	private void init() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean create(String filename, byte[] data, Storage raidType) {
		try {
			int lastVersion = getLastVersion(filename);
			return raidMap.get(raidType.toString()).create(filename + VERSION_SUFFIX + (lastVersion + 1), data);
		} catch (ItemMissingException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean update(String filename, byte[] data) throws ItemMissingException, UserinteractionRequiredException {
		int lastVersion = getLastVersion(filename);
		if (lastVersion == 0) {
			throw new ItemMissingException();
		}

		String fileNameWithVersionSuffix = filename;
		if (!filename.matches(VERSION_REGEX)) {
			fileNameWithVersionSuffix = filename + VERSION_SUFFIX + lastVersion;
		}
		Storage type = getFileStorageType(fileNameWithVersionSuffix);

		return raidMap.get(type).create(filename + VERSION_SUFFIX + (lastVersion + 1), data);
	}

	private Storage getFileStorageType(String fileNameWithVersionSuffix) throws ItemMissingException {
		List<String> raid1FileList = listFiles(Storage.RAID1);
		for (String file : raid1FileList) {
			if (file.equals(fileNameWithVersionSuffix)) {
				return Storage.RAID1;
			}
		}

		List<String> raid5FileList = listFiles(Storage.RAID5);
		for (String file : raid5FileList) {
			if (file.equals(fileNameWithVersionSuffix)) {
				return Storage.RAID5;
			}
		}

		throw new ItemMissingException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File read(String filename) throws ItemMissingException, UserinteractionRequiredException {
		int lastVersion = getLastVersion(filename);
		return read(filename, lastVersion);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File read(String filename, int version) throws ItemMissingException, UserinteractionRequiredException {
		String fileNameWithVersionSuffix = filename;
		if (!filename.matches(VERSION_REGEX)) {
			fileNameWithVersionSuffix = filename + VERSION_SUFFIX + version;
		}
		Storage storageType = getFileStorageType(fileNameWithVersionSuffix);

		return raidMap.get(storageType).read(fileNameWithVersionSuffix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLastVersion(String filename) throws ItemMissingException {
		List<String> fileList = listFiles();

		int lastVersion = 0;
		for (String name : fileList) {
			String actualName = name.split(VERSION_REGEX)[0];

			if (actualName.equals(filename)) {
				int version = extractFileVersion(name);
				if (version > lastVersion) {
					lastVersion = version;
				}
			}
		}
		return lastVersion;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(String filename) throws ItemMissingException {
		int lastVersion = getLastVersion(filename);
		if (lastVersion == 0) {
			throw new ItemMissingException();
		}

		String fileNameWithVersionSuffix = filename;
		if (!filename.matches(VERSION_REGEX)) {
			fileNameWithVersionSuffix = filename + VERSION_SUFFIX + lastVersion;
		}
		Storage storageType = getFileStorageType(fileNameWithVersionSuffix);

		boolean allVersionsDeleted = true;
		for (; lastVersion > 0; lastVersion--) {
			fileNameWithVersionSuffix = filename + VERSION_SUFFIX + lastVersion;
			boolean result = raidMap.get(storageType).delete(fileNameWithVersionSuffix);
			if (!result) {
				allVersionsDeleted = false;
			}
		}

		return allVersionsDeleted;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getFileVersions(String filename) throws ItemMissingException {
		List<String> fileList = listFiles();
		List<String> fileVersionList = new ArrayList<String>();

		for (String name : fileList) {
			String actualName = name.split(VERSION_REGEX)[0];
			if (actualName.equals(filename)) {
				fileVersionList.add(name);
			}
		}
		return fileVersionList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> listFiles() {
		List<String> fileList = listFiles(Storage.RAID1);
		fileList.addAll(listFiles(Storage.RAID5));

		return fileList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> listFiles(Storage storageType) {
		return raidMap.get(storageType.toString()).listFiles();
	}

	private int extractFileVersion(String filename) {
		Pattern pattern = Pattern.compile(VERSION_REGEX);
		Matcher matcher = pattern.matcher(filename);
		if (matcher.find()) {
			if (matcher.group().length() != 0) {
				return Integer.parseInt(matcher.group().substring(2));
			}
		}
		return -1;
	}
}
