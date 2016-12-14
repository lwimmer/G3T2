package at.ac.tuwien.infosys.aic2016.g3t2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;

import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.IBlobstore;
import at.ac.tuwien.infosys.aic2016.g3t2.Blobstore.Location;
import junitx.framework.ListAssert;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestTest {

    @Autowired
    private MockMvc mm;
    
    @Autowired
    private Collection<IBlobstore> blobstores;

    private ObjectMapper om = new ObjectMapper();
    private CollectionLikeType stringListType = getStringListType(om);
    
    @Test
    public void testFileList() throws Exception {
        getFileList();
    }

    @Test
    public void testPutListDelete() throws Exception {
        List<String> initialList = getFileList();

        String filename = putRandomFile().fn;

        List<String> expect = new ArrayList<>(initialList);
        expect.add(filename);

        List<String> list = getFileList();
        ListAssert.assertEquals(expect, list);

        deleteFile(filename);

        list = getFileList();
        ListAssert.assertEquals(initialList, list);
    }
    
    @Test
    public void testRecoverAfterLoss() throws Exception {

        for (IBlobstore bs : blobstores) {
            String bsName = bs.getClass().getSimpleName();
            File file = putRandomFile();
            
            bs.delete(file.fn);
            
            assertTrue(getFileLocations(file.fn)
                    .stream()
                    .allMatch(l -> bsName.equals(l.getBlobstore()) ? l.isRecovered() : ! l.isRecovered()));
            
            byte[] data = getFile(file.fn);
            assertArrayEquals(file.data, data);
            
            deleteFile(file.fn);
        }
    }
    
    @Test
    public void testRecoverAfterModification() throws Exception {

        for (IBlobstore bs : blobstores) {
            String bsName = bs.getClass().getSimpleName();
            File file = putRandomFile();
            
            byte[] secondData = getRandomData(file.data.length);
            bs.create(file.fn, secondData);
            
            assertTrue(getFileLocations(file.fn)
                    .stream()
                    .allMatch(l -> bsName.equals(l.getBlobstore()) ? l.isRecovered() : ! l.isRecovered()));
            
            byte[] data = getFile(file.fn);
            assertArrayEquals(file.data, data);
            
            deleteFile(file.fn);
        }
    }
    
    /*
     * tests:
     * create, delete at one blobstore, recover, delete
     * create, modify at one blobstore, recover, delete
     * create, delete at two blobstores, try recover, delete
     * create, modify at two blobstores, try recover, delete
     * create, delete at one bs, modify at another bs, try recover, delete
     * create at one bs
     * small files, 0 byte size, big files
     */

    private void deleteFile(String filename) throws Exception {
        mm.perform(delete(getUrl(filename))).andExpect(status().isOk()).andExpect(content().string("true"));
    }

    static class File {
        String fn;
        byte[] data;
    }
    
    private File putRandomFile() throws Exception {
        File file = new File();
        file.fn = getRandomFilename();
        file.data = getRandomData();
        putFile(file.fn, file.data);
        return file;
    }
    
    private void putFile(String filename, byte[] data) throws Exception {
        mm.perform(put(getUrl(filename)).content(data)).andExpect(status().isOk()).andExpect(content().string("true"));
    }

    private List<String> getFileList() throws Exception {
        String result = mm.perform(get("/file")).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        return om.readValue(result, stringListType);
    }
    
    private byte[] getFile(String filename) throws Exception {
        return mm.perform(get(getUrl(filename))).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
    }
    
    private List<Location> getFileLocations(String filename) throws Exception {
        String data = mm.perform(get(getUrl(filename) + "/locations")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return om.readValue(data, om.getTypeFactory().constructCollectionLikeType(List.class, Location.class));
    }
    
    private String getUrl(String filename) {
        return "/file/" + filename;
    }
    
    private byte[] getRandomData() {
        return getRandomData(RandomUtils.nextInt(1024) + 1);
    }
    
    private byte[] getRandomData(int size) {
        byte[] data = new byte[size];
        new Random().nextBytes(data);
        return data;
    }
    
    private static CollectionLikeType getStringListType(ObjectMapper om) {
        return om.getTypeFactory().constructCollectionLikeType(List.class, String.class);
    }

    private static String getRandomFilename() {
        return RandomStringUtils.random(10, "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

}
