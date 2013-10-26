package org.hive2hive.core.test.encryption;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKey;

import org.hive2hive.core.encryption.JavaEncryptionUtil;
import org.hive2hive.core.encryption.JavaEncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.encryption.JavaEncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.encryption.ProfileEncryptionUtil;
import org.hive2hive.core.encryption.SignedContent;
import org.hive2hive.core.encryption.UserPassword;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JavaEncryptionUtilTest extends H2HJUnitTest {

	// TODO test exception handling in case wrong encryption keys are used
	JavaEncryptionUtil encryptionUtil = new JavaEncryptionUtil();
	
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = JavaEncryptionUtilTest.class;
		beforeClass();
	}

	@Test
	public void toByteTest() {

		String testString = "abcdefghijklmnopqrstuvwxyzüöä 0123456789";
		byte[] bytes = encryptionUtil.toByte(testString);
		Assert.assertNotNull(bytes);
		Assert.assertEquals(testString, encryptionUtil.toString(bytes));
	}

	@Test
	public void toStringTest() {

		String testString = "abcdefghijklmnopqrstuvwxyzüöä 0123456789";
		byte[] testBytes = encryptionUtil.toByte(testString);
		String result = encryptionUtil.toString(testBytes);
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
				keys[i] = encryptionUtil.createAESKey(sizes[s]);
				Assert.assertNotNull(keys[i]);
				Assert.assertEquals(keys[i].getAlgorithm(), "AES");

				// check key length
				Assert.assertTrue(encryptionUtil.toString(keys[i].getEncoded()).length() == sizes[s].value() / 8);

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
	public void createAESKeyFromPasswordTest() {

		// TODO test whether always the same key is created from the password
		Random random = new Random();

		// check all key sizes
		AES_KEYLENGTH[] sizes = getAESKeySizes();
		for (int s = 0; s < sizes.length; s++) {

			UserPassword[] pws = new UserPassword[50];
			logger.debug(String.format("Testing %s AES keys from password of length %s bits.", pws.length,
					sizes[s].value()));

			for (int i = 0; i < pws.length; i++) {

				// generate user passwords
				pws[i] = ProfileEncryptionUtil.createUserPassword(new BigInteger(130, random).toString(32));

				// generate AES key from password
				SecretKey aesKey = encryptionUtil.createAESKeyFromPassword(pws[i], sizes[s]);

				// check key
				Assert.assertNotNull(aesKey);

				// check key length
				Assert.assertTrue(encryptionUtil.toString(aesKey.getEncoded()).length() == sizes[s].value() / 8);
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
				keyPairs[i] = encryptionUtil.createRSAKeys(sizes[s]);
				Assert.assertNotNull(keyPairs[i]);
				Assert.assertNotNull(keyPairs[i].getPrivate());
				Assert.assertNotNull(keyPairs[i].getPublic());
				Assert.assertEquals(keyPairs[i].getPrivate().getAlgorithm(), "RSA");
				Assert.assertEquals(keyPairs[i].getPublic().getAlgorithm(), "RSA");
			}
		}
	}

//	@Test
//	public void encryptionAESTest() {
//
//		// check all key sizes
//		AES_KEYLENGTH[] sizes = getAESKeySizes();
//
//		for (int s = 0; s < sizes.length; s++) {
//
//			// generate random sized content (max. 5MB)
//			SecureRandom random = new SecureRandom();
//			byte[] content = new byte[random.nextInt(5242880)];
//			random.nextBytes(content);
//
//			// generate AES key
//			SecretKey aesKey = encryptionUtil.createAESKey(sizes[s]);
//
//			logger.debug(String.format("Testing AES encryption of a sample %s byte file with a %s bit key.",
//					content.length, sizes[s].value()));
//
//			// encrypt content
//			EncryptedContent encryptedContent = encryptionUtil.encryptAES(content, aesKey);
//
//			Assert.assertNotNull(encryptedContent);
//			Assert.assertNotNull(encryptedContent.getCipherContent());
//			Assert.assertNotNull(encryptedContent.getInitVector());
//
//			Assert.assertFalse(Arrays.equals(encryptedContent.getCipherContent(), content));
//
//			// decrypt content
//			byte[] decryptedContent = encryptionUtil.decryptAES(encryptedContent, aesKey);
//
//			Assert.assertNotNull(decryptedContent);
//			Assert.assertTrue(Arrays.equals(content, decryptedContent));
//		}
//	}

//	@Test
//	public void encryptionAESWithPassword() {
//
//		// check all key sizes
//		AES_KEYLENGTH[] sizes = getAESKeySizes();
//
//		for (int s = 0; s < sizes.length; s++) {
//
//			// generate random sized content (max. 5MB)
//			SecureRandom random = new SecureRandom();
//			byte[] content = new byte[random.nextInt(5242880)];
//			random.nextBytes(content);
//
//			// generate user password
//			UserPassword upw = ProfileEncryptionUtil.createUserPassword("thisIsAPassword_&123");
//
//			logger.debug(String.format(
//					"Testing AES encryption with password of a sample %s byte file with a %s bit key.",
//					content.length, sizes[s].value()));
//
//			// generate AES key from password
//			SecretKey aesKey = encryptionUtil.createAESKeyFromPassword(upw, sizes[s]);
//
//			// encrypt content
//			EncryptedContent encryptedContent = encryptionUtil.encryptAES(content, aesKey);
//
//			Assert.assertNotNull(encryptedContent);
//			Assert.assertNotNull(encryptedContent.getCipherContent());
//			Assert.assertNotNull(encryptedContent.getInitVector());
//
//			Assert.assertFalse(Arrays.equals(encryptedContent.getCipherContent(), content));
//
//			// decrypt content
//			byte[] decryptedContent = encryptionUtil.decryptAES(encryptedContent, aesKey);
//
//			Assert.assertNotNull(decryptedContent);
//			Assert.assertTrue(Arrays.equals(content, decryptedContent));
//		}
//	}

//	@Test
//	public void encryptionRSATest() {
//
//		// check all key sizes
//		RSA_KEYLENGTH[] sizes = getRSAKeySizes();
//
//		for (int s = 0; s < sizes.length; s++) {
//
//			// generate random sized content (max. (keysize / 8) - 11 bytes)
//			SecureRandom random = new SecureRandom();
//			byte[] content = new byte[random.nextInt((sizes[s].value() / 8) - 11)];
//			random.nextBytes(content);
//
//			// generate RSA key pair
//			KeyPair keyPair = encryptionUtil.createRSAKeys(sizes[s]);
//
//			logger.debug(String.format("Testing RSA encryption of a sample %s byte file with a %s bit key.",
//					content.length, sizes[s].value()));
//
//			// encrypt content with public key
//			EncryptedContent encryptedContent = encryptionUtil.encryptRSA(content, keyPair.getPublic());
//
//			Assert.assertNotNull(encryptedContent);
//			Assert.assertNotNull(encryptedContent.getCipherContent());
//			Assert.assertNull(encryptedContent.getInitVector()); // RSA needs no IV
//
//			Assert.assertFalse(Arrays.equals(encryptedContent.getCipherContent(), content));
//
//			// decrypt content with private key
//			byte[] decryptedContent = encryptionUtil.decryptRSA(encryptedContent, keyPair.getPrivate());
//
//			Assert.assertNotNull(decryptedContent);
//			Assert.assertTrue(Arrays.equals(content, decryptedContent));
//		}
//	}

//	@Test
//	public void encryptStringAESTest() {
//
//		String testString = "abcdefghijklmnopqrstuvwxyzüöä 0123456789";
//		byte[] content = encryptionUtil.serializeObject(testString);
//		SecretKey aesKey = encryptionUtil.createAESKey(AES_KEYLENGTH.BIT_128);
//		EncryptedContent encryptedContent = encryptionUtil.encryptAES(content, aesKey);
//		byte[] decryptedContent = encryptionUtil.decryptAES(encryptedContent, aesKey);
//		String testString2 = (String) encryptionUtil.deserializeObject(decryptedContent);
//
//		Assert.assertEquals(testString, testString2);
//	}

	@Test
	public void signatureTest() {

		// check all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			// generate random sized content (max. 5MB)
			SecureRandom random = new SecureRandom();
			byte[] content = new byte[random.nextInt(5242880)];
			random.nextBytes(content);

			// create an asymmetric key pair
			KeyPair rsaKeyPair = encryptionUtil.createRSAKeys(sizes[s]);

			logger.debug(String.format("Testing signature of a sample %s byte file with a %s bit key.",
					content.length, sizes[s].value()));
			
			// sign the content
			SignedContent signedContent = encryptionUtil.sign(content, rsaKeyPair.getPrivate());
			
			Assert.assertNotNull(signedContent);
			Assert.assertNotNull(signedContent.getOriginalData());
			Assert.assertNotNull(signedContent.getSignatureBytes());
			Assert.assertTrue(Arrays.equals(content, signedContent.getOriginalData()));
			Assert.assertFalse(Arrays.equals(content, signedContent.getSignatureBytes()));
			
			// verify the content
			boolean isVerified = encryptionUtil.verify(signedContent, rsaKeyPair.getPublic());
			
			Assert.assertTrue(isVerified);
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
}
