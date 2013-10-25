package org.hive2hive.core.test.encryption;

import static org.junit.Assert.*;

import javax.crypto.SecretKey;

import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class EncryptionUtilTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = JavaEncryptionUtilTest.class;
		beforeClass();
	}

	@Test
	public void generateAESKeyTest() {

		// test all key sizes
		AES_KEYLENGTH[] sizes = getAESKeySizes();

		for (int s = 0; s < sizes.length; s++) {
			
			logger.debug(String.format("Testing AES %s-bit key generation.", sizes[s].value()));

			SecretKey aesKey = EncryptionUtil.generateAESKey(sizes[s]);

			logger.debug(String.format("Generated AES key: %s", EncryptionUtil.toHex(aesKey.getEncoded())));
			
			assertNotNull(aesKey);
			assertTrue(aesKey.getAlgorithm().equals(EncryptionUtil.AES));
		}
	}

	@Test
	public void encryptionAESTest() {

	}

	private static AES_KEYLENGTH[] getAESKeySizes() {
		AES_KEYLENGTH[] sizes = new AES_KEYLENGTH[AES_KEYLENGTH.values().length];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = AES_KEYLENGTH.values()[i];
		}
		return sizes;
	}
}
