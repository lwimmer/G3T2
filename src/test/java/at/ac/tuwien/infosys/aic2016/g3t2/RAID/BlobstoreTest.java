package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.AWS;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

public class BlobstoreTest {
    private List<IBlobstore> bs;

    @Before
    public void setUp() {
    	this.bs = Arrays.asList(new AWS());
    }

    @After
    public void tearDown() {
    	this.bs = null;
    }

    @Test
    public void create_shouldWorkSimpleCase() throws Exception {
        byte[] data = "blub".getBytes();

        for (IBlobstore bs : this.bs) {
        	assertEquals(false, bs.listBlobs().contains("foo-test-create"));
	        assertEquals(true, bs.create("foo-test-create", data));
	        assertEquals(true, bs.listBlobs().contains("foo-test-create"));
	        assertEquals(true, bs.delete("foo-test-create"));
        }
    }

    @Test
    public void read_shouldWorkSimpleCase() throws Exception {
        byte[] data = "blub".getBytes();

        for (IBlobstore bs : this.bs) {
        	assertEquals(true, bs.create("foo-test-read-1", data));
	        assertArrayEquals(data, bs.read("foo-test-read-1").getData());
	        bs.delete("foo-test-read-1");
        }
    }

    @Test
    public void read_shouldErrorIfMissing() throws Exception {
    	for (IBlobstore bs : this.bs) {
        	assertEquals(false, bs.listBlobs().contains("foo-test-read-missing"));
	        try {
	            bs.read("foo-test-read-missing");
	            fail("should throw exception");
	        } catch (ItemMissingException e) {
	        }
    	}
    }

    @Test
    public void delete_shouldWorkSimpleCase() throws Exception {
    	byte[] data = "blub".getBytes();
    	
    	for (IBlobstore bs : this.bs) {
    		assertEquals(true, bs.create("foo-test-delete-1", data));
	        assertEquals(true, bs.delete("foo-test-delete-1"));
	        assertEquals(Arrays.asList(), bs.listBlobs());
    	}
    }

    @Test
    public void delete_shouldErrorIfMissing() throws Exception {
    	for (IBlobstore bs : this.bs) {
        	assertEquals(false, bs.listBlobs().contains("foo-test-delete-missing"));
    		try {
    			bs.delete("foo-test-delete-missing");
    			fail("should throw exception");
    		} catch (ItemMissingException e) {
    		}
    	}
    }

    @Test
    public void listFiles_shouldWork() throws Exception {
        List<String> list = Arrays.asList("bar-4271", "foo-4271");
        byte[] data = "blub".getBytes();
        
        for (IBlobstore bs : this.bs) {
        	bs.create("bar-4271", data);
        	bs.create("foo-4271", data);
	        assertEquals(list, bs.listBlobs());
	        bs.delete("foo-4271");
	        bs.delete("bar-4271");
        }
    }
}