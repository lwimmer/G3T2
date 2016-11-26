package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import com.dropbox.core.v2.files.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.DbxClientV2;

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;

@Service
@Lazy
public class Dropbox implements IBlobstore {

	private DbxClientV2 dbxClient = null;

	private static final String DEFAULT_STORAGE_PATH = "/";

	@Value("${dropbox.accessToken}") private String accessToken;
	
	@PostConstruct
	private void init() {
		// Reading auth-file
		DbxAuthInfo authInfo = new DbxAuthInfo(accessToken, DbxHost.DEFAULT);
		
		// Creating a DbxClientV2 to make API calls
		DbxRequestConfig requestConfig = new DbxRequestConfig("DropboxAPI/2.1.1");
		dbxClient = new DbxClientV2(requestConfig, authInfo.getAccessToken(), authInfo.getHost());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean create(String blobname, byte[] data) {
		InputStream input = new ByteArrayInputStream(data);

		// Checks if the data is a part of the file. Data is uploaded in a single request.
		try {
			FileMetadata metadata = dbxClient.files().uploadBuilder(DEFAULT_STORAGE_PATH + blobname)
					.withMode(WriteMode.ADD).withClientModified(new Date()).uploadAndFinish(input);
			System.out.println("**********DROPBOX***********");
			System.out.println("Blob is uploaded to Dropbox. \nName: " + blobname + "\nSize: " + metadata.getSize());
			System.out.println("****************************");
		} catch (DbxException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(String blobname) throws ItemMissingException {
		try {
			dbxClient.files().delete(DEFAULT_STORAGE_PATH + blobname);
			System.out.println("**********DROPBOX***********");
			System.out.println("Blob is deleted. \nName: " + blobname);
			System.out.println("****************************");
		} catch (DeleteErrorException e) {
			if (e.errorValue.getPathLookupValue().isNotFound()) {
				throw new ItemMissingException();
			}
			e.printStackTrace();
			return false;
		} catch (DbxException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Blob read(String blobname) throws ItemMissingException {
		try {
			DbxDownloader<FileMetadata> downloader = dbxClient.files().download(DEFAULT_STORAGE_PATH + blobname);
			FileMetadata metadata = downloader.getResult();

			InputStream inputStream = downloader.getInputStream();
			byte[] data = IOUtil.slurp(inputStream, IOUtil.DEFAULT_COPY_BUFFER_SIZE);
			return new Blob(data);
		} catch (DownloadErrorException e) {
			if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
				throw new ItemMissingException();
			}
			e.printStackTrace();
			return null;
		} catch (DbxException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> listBlobs() {
		List<String> blobList = new ArrayList<String>();
		try {
			List<Metadata> list = dbxClient.files().listFolder("").getEntries();
			if (list != null) {
				for (Metadata data : list) {
					blobList.add(data.getName());
				}
			}

		} catch (DbxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return blobList;
	}
}
