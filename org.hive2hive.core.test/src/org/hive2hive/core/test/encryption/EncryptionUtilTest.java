package org.hive2hive.core.test.encryption;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.EncryptionUtil.RSA_KEYLENGTH;
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
	public void generateRSAKeyPairTest() {

		// test all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug(String.format("Testing RSA %s-bit key pair generation.", sizes[s].value()));

			// generate RSA key pair
			KeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(sizes[s]);
			logger.debug(String.format("- Generated Private Key: %s", EncryptionUtil.toHex(rsaKeyPair.getPrivate().getEncoded())));
			logger.debug(String.format("- Generated Public Key:  %s", EncryptionUtil.toHex(rsaKeyPair.getPublic().getEncoded())));

			assertNotNull(rsaKeyPair);
			assertNotNull(rsaKeyPair.getPrivate());
			assertNotNull(rsaKeyPair.getPublic());
			assertTrue(rsaKeyPair.getPrivate().getAlgorithm().equals(EncryptionUtil.RSA));
			assertTrue(rsaKeyPair.getPublic().getAlgorithm().equals(EncryptionUtil.RSA));
		}
	}
	
	@Test
	public void encryptionAESTest() {

		// test all key sizes
		AES_KEYLENGTH[] sizes = getAESKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug(String.format("Testing AES %s-bit encryption and decryption", sizes[s].value()));

			// generate random sized content (max. 2MB)
			byte[] data = generateRandomContent(2097152);
			
			// generate AES key
			SecretKey aesKey = EncryptionUtil.generateAESKey(sizes[s]);

			// generate IV
			byte[] initVector = EncryptionUtil.generateIV();
			
			// encrypt data
			byte[] encryptedData = null;
			try {
				encryptedData = EncryptionUtil.encryptAES(data, aesKey, initVector);
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Exception while testing AES encryption:", e);
				e.printStackTrace();
			}
			
			assertNotNull(encryptedData);
			assertFalse(Arrays.equals(data, encryptedData));
			
			// decrypt data
			byte[] decryptedData = null;
			try {
				decryptedData = EncryptionUtil.decryptAES(encryptedData, aesKey, initVector);
			} catch (DataLengthException | IllegalStateException | InvalidCipherTextException e) {
				logger.error("Exception while testing AES decryption:", e);
				e.printStackTrace();
			}
			
			assertNotNull(decryptedData);
			assertFalse(Arrays.equals(encryptedData, decryptedData));
			assertTrue(Arrays.equals(data, decryptedData));
		}
	}

	private static AES_KEYLENGTH[] getAESKeySizes() {
		AES_KEYLENGTH[] sizes = new AES_KEYLENGTH[AES_KEYLENGTH.values().length];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = AES_KEYLENGTH.values()[i];
		}
		return sizes;
	}

	private static RSA_KEYLENGTH[] getRSAKeySizes() {
		RSA_KEYLENGTH[] sizes = new RSA_KEYLENGTH[RSA_KEYLENGTH.values().length];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = RSA_KEYLENGTH.values()[i];
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
