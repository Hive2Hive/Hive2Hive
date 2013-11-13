package org.hive2hive.core.test.encryption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.hive2hive.core.security.HybridEncryptedContent;
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
			KeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(sizes[s]);

			assertNotNull(rsaKeyPair);
			assertNotNull(rsaKeyPair.getPrivate());
			assertNotNull(rsaKeyPair.getPublic());

			logger.debug(String.format("Private Key: %s",
					EncryptionUtil.toHex(rsaKeyPair.getPrivate().getEncoded())));
			logger.debug(String.format("Public Key: %s",
					EncryptionUtil.toHex(rsaKeyPair.getPublic().getEncoded())));
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
			KeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(sizes[s]);

			// encrypt data with public key
			byte[] encryptedData = null;
			try {
				encryptedData = EncryptionUtil.encryptRSA(data, rsaKeyPair.getPublic());
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
				logger.error("Exception while testing RSA encryption:", e);
				e.printStackTrace();
				e.printStackTrace();
			}

			assertNotNull(encryptedData);
			assertFalse(Arrays.equals(data, encryptedData));

			printBytes("Encrypted Data:", encryptedData);

			// decrypt data with private key
			byte[] decryptedData = null;

			try {
				decryptedData = EncryptionUtil.decryptRSA(encryptedData, rsaKeyPair.getPrivate());
			} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
				logger.error("Exception while testing RSA decryption:", e);
				e.printStackTrace();
			}

			assertNotNull(decryptedData);
			assertTrue(Arrays.equals(data, decryptedData));

			printBytes("Decrypted Data:", decryptedData);
		}
	}

	@Test
	public void encryptionHybridTest() {

		RSA_KEYLENGTH[] rsaSizes = getRSAKeySizes();
		AES_KEYLENGTH[] aesSizes = getAESKeySizes();

		// test all RSA key sizes
		for (int s1 = 0; s1 < rsaSizes.length; s1++) {

			// test all AES key sizes
			for (int s2 = 0; s2 < aesSizes.length; s2++) {

				// generate random content (50 MB)
				byte[] data = generateFixedContent(52428800);

				logger.debug(String
						.format("Testing hybrid encryption and decryption of a sample %s byte file with a %s bit RSA and a %s bit AES key.",
								data.length, rsaSizes[s1].value(), aesSizes[s2].value()));

				// generate RSA key pair
				long start = System.currentTimeMillis();
				KeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(rsaSizes[s1]);
				long stop = System.currentTimeMillis();
				logger.debug(String.format("RSA Key Generation Time: %s ms", stop - start));

				// encrypt data with public key
				HybridEncryptedContent encryptedData = null;
				try {
					start = System.currentTimeMillis();
					encryptedData = EncryptionUtil.encryptHybrid(data, rsaKeyPair.getPublic(), aesSizes[s2]);
					stop = System.currentTimeMillis();
					logger.debug(String.format("Hybrid Encryption Time: %s ms", stop - start));
				} catch (DataLengthException | InvalidKeyException | IllegalStateException
						| InvalidCipherTextException | IllegalBlockSizeException | BadPaddingException e) {
					logger.error("Exception while testing hybrid encryption:", e);
					e.printStackTrace();
				}

				assertNotNull(encryptedData);
				assertNotNull(encryptedData.getEncryptedData());
				assertNotNull(encryptedData.getEncryptedParameters());
				assertFalse(Arrays.equals(data, encryptedData.getEncryptedData()));

				// decrypt data with private key
				byte[] decryptedData = null;
				try {
					start = System.currentTimeMillis();
					decryptedData = EncryptionUtil.decryptHybrid(encryptedData, rsaKeyPair.getPrivate());
					stop = System.currentTimeMillis();
					logger.debug(String.format("Hybrid Decryption Time: %s ms", stop - start));
				} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException
						| BadPaddingException | IllegalStateException | InvalidCipherTextException e) {
					logger.error("Exception while testing hybrid decryption:", e);
					e.printStackTrace();
				}

				assertNotNull(decryptedData);
				assertTrue(Arrays.equals(data, decryptedData));
			}
		}
	}

	@Test
	public void signatureTest() {

		// test all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug(String.format("Testing SHA-1 with RSA %s-bit signing and verificiation.",
					sizes[s].value()));

			// generate random sized content (max. 100 bytes)
			byte[] data = generateRandomContent(100);
			printBytes("Original Data:", data);

			// generate RSA key pair
			KeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(sizes[s]);

			// sign data with private key
			byte[] signature = null;
			try {
				signature = EncryptionUtil.sign(data, rsaKeyPair.getPrivate());
			} catch (InvalidKeyException | SignatureException e) {
				logger.error("Exception while testing signing:", e);
				e.printStackTrace();
			}

			assertNotNull(signature);

			printBytes("Signature:", signature);

			// verify data with public key
			boolean isVerified = false;
			try {
				isVerified = EncryptionUtil.verify(data, signature, rsaKeyPair.getPublic());
			} catch (InvalidKeyException | SignatureException e) {
				logger.error("Exception while testing verification:", e);
				e.printStackTrace();
			}

			assertTrue(isVerified);
		}
	}

	@Test
	public void md5Test() {
		String data = generateRandomString(1000);
		byte[] md5 = EncryptionUtil.generateMD5Hash(data.getBytes());
		assertNotNull(md5);

		// assert that hashing twice results in the same md5 hash
		assertEquals(new String(md5), new String(EncryptionUtil.generateMD5Hash(data.getBytes())));

		// assert that different data is hashed to different md5 hashes
		String data2 = generateRandomString(1000);
		assertNotEquals(data, data2);
		assertNotEquals(new String(md5), new String(EncryptionUtil.generateMD5Hash(data2.getBytes())));
	}

	@Test
	public void serializationTest() {

		String data = generateRandomString(1000);
		logger.debug("Testing data serialization.");
		logger.debug("Test String: " + data);

		byte[] serializedData = EncryptionUtil.serializeObject(data);
		assertNotNull(serializedData);
		printBytes("Serialized Data:", serializedData);

		String deserializedData = (String) EncryptionUtil.deserializeObject(serializedData);
		assertNotNull(deserializedData);
		assertEquals(data, deserializedData);
	}

	public static AES_KEYLENGTH[] getAESKeySizes() {
		AES_KEYLENGTH[] sizes = new AES_KEYLENGTH[AES_KEYLENGTH.values().length];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = AES_KEYLENGTH.values()[i];
		}
		return sizes;
	}

	public static RSA_KEYLENGTH[] getRSAKeySizes() {
		RSA_KEYLENGTH[] sizes = new RSA_KEYLENGTH[RSA_KEYLENGTH.values().length];
		for (int i = 0; i < sizes.length; i++) {
			sizes[i] = RSA_KEYLENGTH.values()[i];
		}
		return sizes;
	}
}
