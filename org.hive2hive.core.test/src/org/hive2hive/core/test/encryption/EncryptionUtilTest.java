package org.hive2hive.core.test.encryption;

import static org.junit.Assert.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
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

			// generate AES key
			SecretKey aesKey = EncryptionUtil.generateAESKey(sizes[s]);
			logger.debug(String.format("Generated AES key: %s", EncryptionUtil.toHex(aesKey.getEncoded())));

			assertNotNull(aesKey);
			assertTrue(aesKey.getAlgorithm().equals(EncryptionUtil.AES));
		}
	}

	@Test
	public void encryptionAESTest() {

		// test all key sizes
		AES_KEYLENGTH[] sizes = getAESKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug(String.format("Testing AES %s-bit encryption:", sizes[s].value()));

			// generate random sized content (max. 2MB)
			byte[] data = generateRandomContent(2097152);
			
			// generate AES key
			SecretKey aesKey = EncryptionUtil.generateAESKey(sizes[s]);

			// encrypt data
			try {
				byte[] encryptedData = EncryptionUtil.encryptAES(data, aesKey);
				
				assertFalse(Arrays.equals(data, encryptedData));

			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Exception while testing AES encryption:", e);
				e.printStackTrace();
			}
		}
	}

	private static AES_KEYLENGTH[] getAESKeySizes() {
		AES_KEYLENGTH[] sizes = new AES_KEYLENGTH[AES_KEYLENGTH.values().length];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = AES_KEYLENGTH.values()[i];
		}
		return sizes;
	}

	private static byte[] generateRandomContent(int sizeInBytes) {
		SecureRandom random = new SecureRandom();
		byte[] content = new byte[random.nextInt(sizeInBytes)];
		random.nextBytes(content);
		return content;
	}
}
