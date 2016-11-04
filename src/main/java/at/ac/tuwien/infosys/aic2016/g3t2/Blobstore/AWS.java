package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.amazonaws.*;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

public class AWS implements IBlobstore {
	private AmazonS3 s3 = null;
	private String bucketName = "aic2016g3t2";
	private com.amazonaws.services.s3.model.Region region = null;
	private Region usWest2 = null;
	
	//Constructor
	public AWS() {
		AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        s3 = new AmazonS3Client(credentials);
        usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);
	}
	/**
     * Create a new item in the S3 and store the passed data in it.
     *
     * @param blobname name of the item
     * @param data data to store
     * @return true if sucessfully stored
     */
    public boolean create(String blobname, byte[] data){
    	if (!s3.doesBucketExist(bucketName)) {
    		s3.createBucket(bucketName);
    	}
    	//check if data empty or can i conjecture that everything is ok (validation by raid class)?
    	
    	ObjectMetadata meta = new ObjectMetadata();
    	InputStream input = new ByteArrayInputStream(data);
    	Date lastMod = new Date();
    	meta.setLastModified(lastMod);
    	
    	//if object already exists, the new one just overwrites the old at the moment. any verifications or confirmation necessary?
    	try {
    		s3.putObject(new PutObjectRequest(bucketName, blobname, input, meta));
    	}
    	
    	catch (Exception e) {
    		return false;
    	}
    	return true;
    }

    /**
     * Delete an item from the S3.
     *
     * @param blobname name of the item
     * @return true if sucessfully deleted
     */
    public boolean delete(String blobname) throws ItemMissingException{
    	if (s3.doesObjectExist(bucketName, blobname)) {
    		s3.deleteObject(bucketName, blobname);
    	}
    	else 
    		throw new ItemMissingException();

    	if (s3.doesObjectExist(bucketName, blobname))
    		return false;
    	else 
    		return true;
    }

    /**
     * Return an item from the S3.
     *
     * @param blobname name of the item
     * @return the item as a Blob
     */
    public Blob read(String blobname) throws ItemMissingException {
    	S3Object s3object = null;
    	if (s3.doesObjectExist(bucketName, blobname)) {
    		s3object = s3.getObject(bucketName, blobname);
    	}
    	else 
    		throw new ItemMissingException();
    	
    	S3ObjectInputStream stream = s3object.getObjectContent();
    	
    	byte[] data = null;
    	try {
			data = IOUtils.toByteArray(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	Blob blob = new Blob();
    	if (data != null) {
    		blob.setData(data);
    		blob.setLocation("aws");
    	}
    	return blob;
    }

    /**
     * Return a list of item names that are saved in the S3.
     * @return list of item names
     */
    public List<String> listBlobs() {
    	List<String> bloblist = new ArrayList<String>();
    	ObjectListing ol = s3.listObjects(bucketName);
    	List<S3ObjectSummary> summary = ol.getObjectSummaries();
    	for (S3ObjectSummary os : summary) {
    		bloblist.add(os.getKey());
    	}
    	return bloblist;
    }
}
