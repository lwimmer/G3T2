package at.ac.tuwien.infosys.aic2016.g3t2.RAID;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Blob;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;
import at.ac.tuwien.infosys.aic2016.g3t2.exceptions.ItemMissingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class RAID1Test {
    private IBlobstore bs1;
    private IBlobstore bs2;
    private IBlobstore bs3;

    @Before
    public void setUp() {
        this.bs1 = Mockito.mock(IBlobstore.class);
        this.bs2 = Mockito.mock(IBlobstore.class);
        this.bs3 = Mockito.mock(IBlobstore.class);
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(bs1);
        Mockito.verifyNoMoreInteractions(bs2);
        Mockito.verifyNoMoreInteractions(bs3);

        bs1 = null;
        bs2 = null;
        bs3 = null;
    }

    @Test
    public void create_shouldWorkSimpleCase() throws Exception {
        byte[] data = "blub".getBytes();

        RAID1 r = new RAID1(Arrays.asList(bs1));

        Mockito.when(bs1.create("foo", data)).thenReturn(true);
        assertEquals(true, r.create("foo", data));
        Mockito.verify(bs1).create("foo", data);
    }

    @Test
    public void create_shouldWorkIfSomeBlobstoresFail() throws Exception {
        byte[] data = "blub".getBytes();

        RAID1 r = new RAID1(Arrays.asList(bs1, bs2, bs3));

        Mockito.when(bs1.create("foo", data)).thenReturn(false);
        Mockito.when(bs2.create("foo", data)).thenReturn(true);
        Mockito.when(bs3.create("foo", data)).thenReturn(false);

        assertEquals(true, r.create("foo", data));

        Mockito.verify(bs1).create("foo", data);
        Mockito.verify(bs2).create("foo", data);
        Mockito.verify(bs3).create("foo", data);
    }
    @Test
    public void create_shouldErrorIfAllBlobstoresFail() throws Exception {
        byte[] data = "blub".getBytes();

        RAID1 r = new RAID1(Arrays.asList(bs1, bs2));

        Mockito.when(bs1.create("foo", data)).thenReturn(false);
        Mockito.when(bs2.create("foo", data)).thenReturn(false);

        assertEquals(false, r.create("foo", data));

        Mockito.verify(bs1).create("foo", data);
        Mockito.verify(bs2).create("foo", data);
    }

    @Test
    public void read_shouldWorkSimpleCase() throws Exception {
        byte[] data = "blub".getBytes();
        Blob blob = new Blob(data);

        RAID1 r = new RAID1(Arrays.asList(bs1));

        Mockito.when(bs1.read("foo")).thenReturn(blob);

        File f = r.read("foo");
        assertEquals(data, f.getData());
        assertEquals("foo", f.getLocations().get(0).getFilename());
        assertEquals(true, f.getLocations().get(0).isOriginal());

        Mockito.verify(bs1).read("foo");
    }

    @Test
    public void read_shouldWorkIfMissingOnSomeBlobstores() throws Exception {
        byte[] data = "blub".getBytes();
        Blob blob = new Blob(data);

        RAID1 r = new RAID1(Arrays.asList(bs1, bs2, bs3));

        Mockito.when(bs1.read("foo")).thenThrow(new ItemMissingException());
        Mockito.when(bs2.read("foo")).thenReturn(blob);
        Mockito.when(bs3.read("foo")).thenThrow(new ItemMissingException());

        File f = r.read("foo");
        assertEquals(data, f.getData());
        assertEquals("foo", f.getLocations().get(0).getFilename());
        assertEquals(true, f.getLocations().get(0).isOriginal());
        assertEquals(false, f.getLocations().get(0).isRecovered());

        assertEquals("foo", f.getLocations().get(1).getFilename());
        assertEquals(true, f.getLocations().get(1).isOriginal());
        assertEquals(true, f.getLocations().get(1).isRecovered());

        assertEquals("foo", f.getLocations().get(2).getFilename());
        assertEquals(true, f.getLocations().get(2).isOriginal());
        assertEquals(true, f.getLocations().get(2).isRecovered());

        Mockito.verify(bs1).read("foo");
        // data missing, should restore
        Mockito.verify(bs1).create("foo", data);
        Mockito.verify(bs2).read("foo");
        Mockito.verify(bs3).read("foo");
        // data missing, should restore
        Mockito.verify(bs3).create("foo", data);
    }

    @Test
    public void read_shouldErrorIfAllMissing() throws Exception {
        RAID1 r = new RAID1(Arrays.asList(bs1));

        Mockito.when(bs1.read("foo2")).thenThrow(new ItemMissingException());

        try {
            r.read("foo2");
            fail("should throw exception");
        } catch (ItemMissingException e) {
        }

        Mockito.verify(bs1).read("foo2");
    }

    @Test
    public void delete_shouldWorkSimpleCase() throws Exception {
        RAID1 r = new RAID1(Arrays.asList(bs1, bs2, bs3));

        Mockito.when(bs1.delete("foo2")).thenReturn(true);
        Mockito.when(bs2.delete("foo2")).thenReturn(true);
        Mockito.when(bs3.delete("foo2")).thenReturn(true);

        assertEquals(true, r.delete("foo2"));

        Mockito.verify(bs1).delete("foo2");
        Mockito.verify(bs2).delete("foo2");
        Mockito.verify(bs3).delete("foo2");
    }

    @Test
    public void delete_shouldIgnoreNotAllMissing() throws Exception {
        RAID1 r = new RAID1(Arrays.asList(bs1, bs2));

        Mockito.when(bs1.delete("foo2")).thenThrow(new ItemMissingException());
        Mockito.when(bs2.delete("foo2")).thenReturn(true);

        assertEquals(true, r.delete("foo2"));

        Mockito.verify(bs1).delete("foo2");
        Mockito.verify(bs2).delete("foo2");
    }
    @Test
    public void delete_shouldErrorFirstFailure() throws Exception {
        RAID1 r = new RAID1(Arrays.asList(bs1, bs2));

        Mockito.when(bs1.delete("foo2")).thenReturn(false);
        Mockito.when(bs2.delete("foo2")).thenReturn(false);

        assertEquals(false, r.delete("foo2"));

        Mockito.verify(bs1).delete("foo2");
        Mockito.verify(bs2, times(0)).delete("foo2");
    }

    @Test
    public void delete_shouldErrorAllMissing() throws Exception {
        RAID1 r = new RAID1(Arrays.asList(bs1, bs2));

        Mockito.when(bs1.delete("foo2")).thenThrow(new ItemMissingException());
        Mockito.when(bs2.delete("foo2")).thenThrow(new ItemMissingException());

        try {
            r.delete("foo2");
            fail("Should throw exception");
        } catch (ItemMissingException e) {
        }

        Mockito.verify(bs1).delete("foo2");
        Mockito.verify(bs2).delete("foo2");
    }

    @Test
    public void listFiles_shouldWork() throws Exception {
        List<String> list1 = Arrays.asList();
        List<String> list2 = Arrays.asList("foo", "bar");
        List<String> list3 = Arrays.asList("blub", "bar");
        List<String> result = Arrays.asList("foo", "bar", "blub");

        RAID1 r = new RAID1(Arrays.asList(bs1, bs2, bs3));
        Mockito.when(bs1.listBlobs()).thenReturn(list1);
        Mockito.when(bs2.listBlobs()).thenReturn(list2);
        Mockito.when(bs3.listBlobs()).thenReturn(list3);

        assertEquals(result, r.listFiles());

        Mockito.verify(bs1).listBlobs();
        Mockito.verify(bs2).listBlobs();
        Mockito.verify(bs3).listBlobs();
    }
}