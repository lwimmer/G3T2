package at.ac.tuwien.infosys.aic2016.g3t2.Blobstore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.BasicAWSCredentials;
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

import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;

@Service
@Lazy
public class AWS implements IBlobstore {
	
	private AmazonS3 s3 = null;
	
	@Value("${aws.region}") private String regionStr;

	@Value("${aws.bucketName}") private String bucketName;
	
	@Value("${aws.accessKeyId}") private String accessKeyId;
	
	@Value("${aws.secretKey}") private String secretKey;
	
	@PostConstruct
	private void init() {
        s3 = new AmazonS3Client(new BasicAWSCredentials(accessKeyId, secretKey));
        s3.setRegion(Region.getRegion(Regions.fromName(regionStr)));
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
