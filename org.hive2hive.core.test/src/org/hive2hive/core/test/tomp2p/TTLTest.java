package org.hive2hive.core.test.tomp2p;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A test which test the content protection mechanisms of <code>TomP2P</code>. Tests should be completely
 * independent of <code>Hive2Hive</code>.
 * 
 * @author Seppi
 */
public class TTLTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = TTLTest.class;
		beforeClass();
	}

	@Ignore
	@Test
	public void test() throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException {

	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
