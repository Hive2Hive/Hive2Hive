package org.hive2hive.core.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.security.EncryptionUtil.RSA_KEYLENGTH;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

			logger.debug("Testing AES {}-bit key generation.", sizes[s].value());

			// generate AES key
			long start = System.currentTimeMillis();
			SecretKey aesKey = EncryptionUtil.generateAESKey(sizes[s]);
			long stop = System.currentTimeMillis();
			logger.debug("Generated AES key: {}.", EncryptionUtil.byteToHex(aesKey.getEncoded()));
			logger.debug("Time: {} ms.", stop - start);

			assertNotNull(aesKey);
			assertTrue(aesKey.getAlgorithm().equals("AES"));
		}
	}

	@Test
	public void generateRSAKeyPairTest() {

		// test all key sizes
		RSA_KEYLENGTH[] sizes = getRSAKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug("Testing RSA {}-bit key pair generation.", sizes[s].value());

			// generate RSA key pair
			long start = System.currentTimeMillis();
			KeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(sizes[s]);
			long stop = System.currentTimeMillis();

			assertNotNull(rsaKeyPair);
			assertNotNull(rsaKeyPair.getPrivate());
			assertNotNull(rsaKeyPair.getPublic());

			logger.debug("Private Key: {}.", EncryptionUtil.byteToHex(rsaKeyPair.getPrivate().getEncoded()));
			logger.debug("Public Key: {}.", EncryptionUtil.byteToHex(rsaKeyPair.getPublic().getEncoded()));
			logger.debug("Time: {} ms.", stop - start);
		}
	}

	@Test
	public void encryptionAESTest() {

		// test all key sizes
		AES_KEYLENGTH[] sizes = getAESKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			logger.debug("Testing AES {}-bit encryption and decryption.", sizes[s].value());

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

			logger.debug("Testing RSA {}-bit encryption and decryption.", sizes[s].value());

			// generate random sized content (max. (key size / 8) - 11 bytes)
			byte[] data = generateRandomContent((sizes[s].value() / 8) - 11);

			logger.debug("Testing RSA encryption of a sample {} byte file with a {} bit key.", data.length, sizes[s].value());
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
			assertNotEquals(0, encryptedData.length);
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
			assertNotEquals(0, decryptedData.length);
			assertTrue(Arrays.equals(data, decryptedData));

			printBytes("Decrypted Data:", decryptedData);
		}
	}

	@Test
	public void encryptionHybridTest() {
		Random rnd = new Random();
		RSA_KEYLENGTH[] rsaSizes = getRSAKeySizes();
		AES_KEYLENGTH[] aesSizes = getAESKeySizes();

		// test all RSA key sizes
		for (int s1 = 0; s1 < rsaSizes.length; s1++) {

			// test all AES key sizes
			for (int s2 = 0; s2 < aesSizes.length; s2++) {

				// generate random content (0-10 MB)
				byte[] data = generateFixedContent((int) (rnd.nextDouble() * 10 * 1024 * 1024));

				logger.debug(
						"Testing hybrid encryption and decryption of a sample {} byte file with a {} bit RSA and a {} bit AES key.",
						data.length, rsaSizes[s1].value(), aesSizes[s2].value());

				// generate RSA key pair
				long start = System.currentTimeMillis();
				KeyPair rsaKeyPair = EncryptionUtil.generateRSAKeyPair(rsaSizes[s1]);
				long stop = System.currentTimeMillis();
				logger.debug("RSA Key Generation Time: {} ms.", stop - start);

				// encrypt data with public key
				HybridEncryptedContent encryptedData = null;
				try {
					start = System.currentTimeMillis();
					encryptedData = EncryptionUtil.encryptHybrid(data, rsaKeyPair.getPublic(), aesSizes[s2]);
					stop = System.currentTimeMillis();
					logger.debug("Hybrid Encryption Time: {} ms.", stop - start);
				} catch (DataLengthException | InvalidKeyException | IllegalStateException | InvalidCipherTextException
						| IllegalBlockSizeException | BadPaddingException e) {
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
					logger.debug("Hybrid Decryption Time: {} ms.", stop - start);
				} catch (InvalidKeyException | DataLengthException | IllegalBlockSizeException | BadPaddingException
						| IllegalStateException | InvalidCipherTextException e) {
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

			logger.debug("Testing SHA-1 with RSA {}-bit signing and verificiation.", sizes[s].value());

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
	public void testIVGeneration() {
		for (int i = 0; i < 100000; i++)
			Assert.assertNotEquals(0, EncryptionUtil.generateIV()[0]);
	}

	@Test
	@Ignore
	public void testPureLightweightBouncyCastle() throws IOException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, DataLengthException, IllegalStateException, InvalidCipherTextException,
			NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException {

		long startTime = System.currentTimeMillis();

		Security.addProvider(new BouncyCastleProvider());

		// generate RSA keys
		RSAKeyPairGenerator gen = new RSAKeyPairGenerator();
		gen.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16), new SecureRandom(), 2048, 80));
		AsymmetricCipherKeyPair keyPair = gen.generateKeyPair();

		// some data where first entry is 0
		byte[] data = { 10, 122, 12, 127, 35, 58, 87, 56, -6, 73, 10, -13, -78, 4, -122, -61 };

		// encrypt data asymmetrically
		AsymmetricBlockCipher cipher = new RSAEngine();
		cipher = new PKCS1Encoding(cipher);
		cipher.init(true, keyPair.getPublic());
		byte[] rsaEncryptedData = cipher.processBlock(data, 0, data.length);

		Assert.assertFalse(Arrays.equals(data, rsaEncryptedData));

		// decrypt data asymmetrically
		cipher.init(false, keyPair.getPrivate());
		byte[] dataBack = cipher.processBlock(rsaEncryptedData, 0, rsaEncryptedData.length);

		assertTrue(Arrays.equals(data, dataBack));

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		logger.debug("elapsed time = {}", elapsedTime);
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

	@AfterClass
	public static void endTest() throws Exception {
		afterClass();
	}
}
