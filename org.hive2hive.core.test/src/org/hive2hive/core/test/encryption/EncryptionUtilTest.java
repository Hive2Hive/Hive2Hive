package org.hive2hive.core.test.encryption;

import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKey;

import org.hive2hive.core.encryption.EncryptedContent;
import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class EncryptionUtilTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = EncryptionUtilTest.class;
		beforeClass();
	}

	@Test
	public void serializationTest() {

		// test String serialization
		String testString = "abcdefghijklmnopqrstuvwxyzüöä 0123456789";
		byte[] serialized = EncryptionUtil.serializeObject(testString);
		Assert.assertNotEquals(testString, EncryptionUtil.toString(serialized));

		String deserialized = (String) EncryptionUtil.deserializeObject(serialized);
		Assert.assertEquals(testString, deserialized);

		// TODO test Object serialization
	}

	@Test
	public void toByteTest() {

		String testString = "abcdefghijklmnopqrstuvwxyzüöä 0123456789";
		byte[] bytes = EncryptionUtil.toByte(testString);
		Assert.assertNotNull(bytes);
		Assert.assertEquals(testString, EncryptionUtil.toString(bytes));
	}

	@Test
	public void toStringTest() {

		String testString = "abcdefghijklmnopqrstuvwxyzüöä 0123456789";
		byte[] testBytes = EncryptionUtil.toByte(testString);
		String result = EncryptionUtil.toString(testBytes);
		Assert.assertNotNull(result);
		Assert.assertEquals(testString, result);
	}

	@Test
	public void createAESKeyTest() {

		// check all key sizes
		AES_KEYLENGTH[] sizes = getAESKeySizes();
		for (int s = 0; s < sizes.length; s++) {

			SecretKey[] keys = new SecretKey[50];
			logger.debug(String.format("Testing %s AES keys of length %s bits.", keys.length,
					sizes[s].value()));

			for (int i = 0; i < keys.length; i++) {

				// check key
				keys[i] = EncryptionUtil.createAESKey(sizes[s]);
				Assert.assertNotNull(keys[i]);
				Assert.assertEquals(keys[i].getAlgorithm(), "AES");

				// check key length
				Assert.assertTrue(EncryptionUtil.toString(keys[i].getEncoded()).length() == sizes[s].value() / 8);

				// // check for variety of key
				// for (int j = 0; j < i; j++) {
				// Assert.assertNotEquals(keys[i], keys[j]);
				// Assert.assertNotEquals(keys[i].getEncoded(), keys[j].getEncoded());
				// Assert.assertNotEquals(keys[i].toString(), keys[j].toString());
				// }
			}
		}
	}

	@Test
	public void createRSAKeysTest() {

		// check all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();
		for (int s = 0; s < sizes.length; s++) {

			KeyPair[] keyPairs = new KeyPair[10];
			logger.debug(String.format("Testing %s RSA key pairs of length %s bits.", keyPairs.length,
					sizes[s].value()));

			for (int i = 0; i < keyPairs.length; i++) {

				// check key
				keyPairs[i] = EncryptionUtil.createRSAKeys(sizes[s]);
				Assert.assertNotNull(keyPairs[i]);
				Assert.assertEquals(keyPairs[i].getPrivate().getAlgorithm(), "RSA");
				Assert.assertEquals(keyPairs[i].getPublic().getAlgorithm(), "RSA");

				// check key length
				Assert.assertTrue(EncryptionUtil.toString(keyPairs[i].getPrivate().getEncoded()).length() == sizes[s]
						.value() / 8);
				Assert.assertTrue(EncryptionUtil.toString(keyPairs[i].getPublic().getEncoded()).length() == sizes[s]
						.value() / 8);
			}
		}
	}

	@Test
	public void encryptionAESTest() {

		// check all key sizes
		AES_KEYLENGTH[] sizes = getAESKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			// generate random sized content (max. 5MB)
			SecureRandom random = new SecureRandom();
			byte[] content = new byte[random.nextInt(5242880)];
			random.nextBytes(content);
			
			logger.debug(String.format("Testing AES encryption of %s byte file with a %s bit key.",
					content.length, sizes[s].value()));
			
			// generate AES key
			SecretKey aesKey = EncryptionUtil.createAESKey(sizes[s]);

			// encrypt content
			EncryptedContent encryptedContent = EncryptionUtil.encryptAES(content, aesKey);

			Assert.assertNotNull(encryptedContent);
			Assert.assertNotNull(encryptedContent.getContent());
			Assert.assertNotNull(encryptedContent.getInitVector());

			Assert.assertFalse(Arrays.equals(encryptedContent.getContent(), content));

			// decrypt content
			byte[] decryptedContent = EncryptionUtil.decryptAES(encryptedContent, aesKey);

			Assert.assertNotNull(decryptedContent);
			Assert.assertTrue(Arrays.equals(content, decryptedContent));
		}
	}

	@Test
	public void encryptionRSATest() {

		// check all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();

		for (int s = 0; s < sizes.length; s++) {
			
			// generate random sized content (max. (keysize / 8) - 11 bytes)
			SecureRandom random = new SecureRandom();
			byte[] content = new byte[random.nextInt((sizes[s].value()/8)-11)];
			random.nextBytes(content);
			
			logger.debug(String.format("Testing RSA encryption of %s byte file with a %s bit key.",
					content.length, sizes[s].value()));
			
			// generate RSA key pair
			KeyPair keyPair = EncryptionUtil.createRSAKeys(sizes[s]);
			
			// encrypt content with public key
			EncryptedContent encryptedContent = EncryptionUtil.encryptRSA(content, keyPair.getPublic());
			
			Assert.assertNotNull(encryptedContent);
			Assert.assertNotNull(encryptedContent.getContent());
			Assert.assertNull(encryptedContent.getInitVector()); // RSA needs no IV
			
			Assert.assertFalse(Arrays.equals(encryptedContent.getContent(), content));
			
			// decrypt content with private key
			byte[] decryptedContent = EncryptionUtil.decryptRSA(encryptedContent, keyPair.getPrivate());
			
			Assert.assertNotNull(decryptedContent);
			Assert.assertTrue(Arrays.equals(content, decryptedContent));
		}
	}

	@Test
	public void encryptStringAESTest() {

		String testString = "abcdefghijklmnopqrstuvwxyzüöä 0123456789";
		byte[] content = EncryptionUtil.serializeObject(testString);
		SecretKey aesKey = EncryptionUtil.createAESKey(AES_KEYLENGTH.BIT_128);
		EncryptedContent encryptedContent = EncryptionUtil.encryptAES(content, aesKey);
		byte[] decryptedContent = EncryptionUtil.decryptAES(encryptedContent, aesKey);
		String testString2 = (String) EncryptionUtil.deserializeObject(decryptedContent);

		Assert.assertEquals(testString, testString2);
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
}
