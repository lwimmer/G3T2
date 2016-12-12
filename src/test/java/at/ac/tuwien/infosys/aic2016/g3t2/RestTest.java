package at.ac.tuwien.infosys.aic2016.g3t2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
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

import junitx.framework.ListAssert;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestTest {

    @Autowired
    private MockMvc mm;

    @Test
    public void testFileList() throws Exception {
        mm.perform(get("/file")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testPut() throws Exception {
        ObjectMapper om = new ObjectMapper();
        CollectionLikeType stringListType = getStringListType(om);

        String result = mm.perform(get("/file")).andReturn().getResponse().getContentAsString();
        List<String> initialList = om.readValue(result, stringListType);

        String rand = getRandomFilename();
        byte[] data = new byte[RandomUtils.nextInt(1024) + 1];
        new Random().nextBytes(data);

        String url = "/file/" + rand;
        mm.perform(put(url).content(data)).andExpect(status().isOk()).andExpect(content().string("true"));

        final List<String> expect = new ArrayList<>(initialList);
        expect.add(rand);

        result = mm.perform(get("/file")).andReturn().getResponse().getContentAsString();
        List<String> list = om.readValue(result, stringListType);
        ListAssert.assertEquals(expect, list);

        mm.perform(delete(url)).andExpect(status().isOk()).andExpect(content().string("true"));

        result = mm.perform(get("/file")).andReturn().getResponse().getContentAsString();
        list = om.readValue(result, stringListType);
        ListAssert.assertEquals(initialList, list);

    }

    private static CollectionLikeType getStringListType(ObjectMapper om) {
        return om.getTypeFactory().constructCollectionLikeType(List.class, String.class);
    }

    private static String getRandomFilename() {
        return RandomStringUtils.random(10, "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

}
