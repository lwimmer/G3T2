package at.ac.tuwien.infosys.aic2016.g3t2;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		assertEquals(1, 1);
	}

}
