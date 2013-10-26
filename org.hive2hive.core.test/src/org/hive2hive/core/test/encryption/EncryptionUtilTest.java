package org.hive2hive.core.test.encryption;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
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
		testClass = EncryptionUtilTest.class;
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
			assertTrue(aesKey.getAlgorithm().equals("AES"));
		}
	}

	@Test
	public void generateRSAKeyPairTest() {

		// test all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug(String.format("Testing RSA %s-bit key pair generation.", sizes[s].value()));

			// generate RSA key pair
			AsymmetricCipherKeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(sizes[s]);

			assertNotNull(rsaKeyPair);
			assertNotNull(rsaKeyPair.getPrivate());
			assertNotNull(rsaKeyPair.getPublic());
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

	@Test
	public void encryptionRSATest() {

		// test all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug(String.format("Testing RSA %s-bit encryption and decryption", sizes[s].value()));

			// generate random sized content (max. (key size / 8) - 11 bytes)
			byte[] data = generateRandomContent((sizes[s].value() / 8) - 11);

			logger.debug(String.format("Testing RSA encryption of a sample %s byte file with a %s bit key.",
					data.length, sizes[s].value()));
			printBytes("Original Data", data);

			// generate RSA key pair
			AsymmetricCipherKeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(sizes[s]);

			// encrypt data with public key
			byte[] encryptedData = null;
			try {
				encryptedData = EncryptionUtil.encryptRSA(data, rsaKeyPair.getPublic());
			} catch (InvalidCipherTextException e) {
				logger.error("Exception while testing RSA encryption:", e);
				e.printStackTrace();
			}

			assertNotNull(encryptedData);
			assertFalse(Arrays.equals(data, encryptedData));

			printBytes("Encrypted Data:", encryptedData);

			// decrypt data with private key
			byte[] decryptedData = null;
			try {
				decryptedData = EncryptionUtil.decryptRSA(encryptedData, rsaKeyPair.getPrivate());
			} catch (InvalidCipherTextException e) {
				logger.error("Exception while testing RSA decryption:", e);
				e.printStackTrace();
			}

			assertNotNull(decryptedData);
			assertTrue(Arrays.equals(data, decryptedData));

			printBytes("Decrypted Data:", decryptedData);
		}
	}

	@Test
	public void signatureTest() {

		// test all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug(String.format("Testing %s-bit RSA SHA-1 signing and verificiation.", sizes[s].value()));

			// generate random sized content (max. 100 bytes)
			byte[] data = generateRandomContent(100);
			printBytes("Original Data", data);

			// generate RSA key pair
			AsymmetricCipherKeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(sizes[s]);

			// sign data with private key
			byte[] signedData = null;
			try {
				signedData = EncryptionUtil.sign(data, rsaKeyPair.getPrivate());
			} catch (DataLengthException | CryptoException e) {
				logger.error("Exception while testing signing:", e);
				e.printStackTrace();
			}
			
			assertNotNull(signedData);
			assertFalse(Arrays.equals(data, signedData));

			printBytes("Signed Data:", signedData);
		}
	}

	@Test
	public void serializationTest() {

		String data = generateRandomString();
		logger.debug("Testing data serialization.");
		logger.debug("Test String: " + data);

		byte[] serializedData = EncryptionUtil.serializeObject(data);
		assertNotNull(serializedData);
		printBytes("Serialized Data:", serializedData);

		String deserializedData = (String) EncryptionUtil.deserializeObject(serializedData);
		assertNotNull(deserializedData);
		assertEquals(data, deserializedData);
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

	private static String generateRandomString() {
		return new BigInteger(130, new Random()).toString(32);
	}

	private static void printBytes(String description, byte[] bytes) {
		logger.debug(description);
		logger.debug(EncryptionUtil.toHex(bytes));
	}
}
