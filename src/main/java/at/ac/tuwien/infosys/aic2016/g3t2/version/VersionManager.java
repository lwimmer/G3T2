package at.ac.tuwien.infosys.aic2016.g3t2.version;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.infosys.aic2016.g3t2.RAID.File;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.RAIDSwitch;
import at.ac.tuwien.infosys.aic2016.g3t2.RAID.RAIDType;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.UserinteractionRequiredException;

@Service
public class VersionManager implements IVersionManager {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String VERSION_REGEX = "_[v][0-9]{0,2}$";

	private final String VERSION_SUFFIX = "_v";

	@Autowired
	private RAIDSwitch raid;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean create(String storagefilename, byte[] data) {
		return create(storagefilename, data, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean create(String filename, byte[] data, RAIDType raidType) {
		try {
			int lastVersion = getLastVersion(filename);
			return raid.create(filename + VERSION_SUFFIX + (lastVersion + 1), data, raidType);
		} catch (ItemMissingException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File read(String filename) throws ItemMissingException, UserinteractionRequiredException {
		int lastVersion = getLastVersion(filename);
		File file = read(filename, lastVersion);
		file.getMetadata().setVersions(getFileVersions(filename));
		return file;
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
		return raid.read(fileNameWithVersionSuffix);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getLastVersion(String filename) throws ItemMissingException {
		List<String> fileList = raid.listFiles();

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
		boolean allVersionsDeleted = true;
		for (; lastVersion > 0; lastVersion--) {
			fileNameWithVersionSuffix = filename + VERSION_SUFFIX + lastVersion;
			boolean result = raid.delete(fileNameWithVersionSuffix);
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
	public List<Integer> getFileVersions(String filename) throws ItemMissingException {
		List<String> fileList = raid.listFiles();
		List<Integer> fileVersionList = new ArrayList<Integer>();

		for (String name : fileList) {
			String actualName = name.split(VERSION_REGEX)[0];
			if (actualName.equals(filename)) {
				int version = extractFileVersion(name);
				fileVersionList.add(version);
			}
		}
		return fileVersionList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> listFiles() {
		return raid.listFiles().stream().map(n -> n.replaceFirst(VERSION_REGEX, "")).distinct()
				.collect(Collectors.toList());
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
