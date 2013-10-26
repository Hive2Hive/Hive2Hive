package org.hive2hive.core.encryption;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class EncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(EncryptionUtil.class);

	private static String digits = "0123456789abcdef";

	private static final String BC = "BC";
	public static final String AES = "AES";
	public static final String RSA = "RSA";
	private static final String AES_CBC_PKCS7PADDING = "AES/CBC/PKCS7Padding";

	public enum AES_KEYLENGTH {
		BIT_128(128),
		BIT192(192),
		BIT256(256);

		private final int bitLength;

		AES_KEYLENGTH(int length) {
			bitLength = length;
		}

		public int value() {
			return bitLength;
		}
	}

	public enum RSA_KEYLENGTH {
		BIT_1024(1024),
		BIT_2048(2048),
		BIT_4096(4096);

		private final int bitLength;

		RSA_KEYLENGTH(int length) {
			bitLength = length;
		}

		public int value() {
			return bitLength;
		}
	}

	private EncryptionUtil() {
	}

	public static byte[] generateIV() {
		SecureRandom random = new SecureRandom();
		byte[] iv = new byte[16];
		random.nextBytes(iv);
		return iv;
	}

	public static SecretKey generateAESKey(AES_KEYLENGTH keyLength) {

		installBCProvider();

		try {
			final KeyGenerator kg = KeyGenerator.getInstance(AES, BC);
			kg.init(keyLength.value(), new SecureRandom());
			byte[] encoded = kg.generateKey().getEncoded();
			return new SecretKeySpec(encoded, AES);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			logger.error("Exception while AES key generator instance creation:", e);
		}
		return null;
	}

	public static AsymmetricCipherKeyPair generateRSAKeyPair(RSA_KEYLENGTH keyLength) {

		BigInteger publicExp = new BigInteger("10001", 16); // Fermat F4, largest known fermat prime
		int strength = keyLength.value();
		int certainty = 80; // certainty for the numbers to be primes, values >80 slow down algorithm
		
		RSAKeyPairGenerator kpg = new RSAKeyPairGenerator();
		KeyGenerationParameters parameters = new RSAKeyGenerationParameters(publicExp, new SecureRandom(), strength, certainty);
		kpg.init(parameters);

		AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();
		return keyPair;

		// try {
		// final KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA, BC);
		// kpg.initialize(keyLength.value(), new SecureRandom());
		// return kpg.generateKeyPair();
		// } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
		// logger.error("Exception while RSA key generator instance creation:", e);
		// }
		// return null;
	}

	// public static EncryptedContent encryptAES(byte[] data, SecretKey secretKey) throws InvalidKeyException,
	// InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
	// return encryptAES(data, generateIV(), secretKey);
	// }
	//
	// public static EncryptedContent encryptAES(byte[] data, byte[] initVector, SecretKey secretKey)
	// throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
	// BadPaddingException {
	//
	// // initialize the initialization vector (IV)
	// IvParameterSpec ivSpec = new IvParameterSpec(initVector);
	//
	// // create cipher instance
	// try {
	// Cipher cipher = Cipher.getInstance(AES_CBC_PKCS7PADDING, BC);
	//
	// // initialize cipher
	// cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
	//
	// // encrypt data
	// byte[] cipherContent = cipher.doFinal(data);
	//
	// return new EncryptedContent(cipherContent, initVector);
	//
	// } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
	// logger.error("Exception while cipher instance creation:", e);
	// }
	// return null;
	// }

	public static byte[] encryptAES(byte[] data, SecretKey secretKey, byte[] initVector)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {

		AESEngine aesEngine = new AESEngine();
		CBCBlockCipher cbc = new CBCBlockCipher(aesEngine);
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc);

		CipherParameters parameters = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()),
				initVector);
		cipher.init(true, parameters);

		return cipherData(cipher, data);
	}

	public static byte[] decryptAES(byte[] data, SecretKey secretKey, byte[] initVector)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {

		AESEngine aesEngine = new AESEngine();
		CBCBlockCipher cbc = new CBCBlockCipher(aesEngine);
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc);

		CipherParameters parameters = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()),
				initVector);
		cipher.init(false, parameters);

		return cipherData(cipher, data);
	}

	public static byte[] encryptRSA(byte[] data, CipherParameters key) throws InvalidCipherTextException {

		// String value = "";
		// String key = readFileAsString(publicKeyFilename);
		// BASE64Decoder b64 = new BASE64Decoder();
		// AsymmetricKeyParameter publicKey =
		// (AsymmetricKeyParameter) PublicKeyFactory.createKey(b64.decodeBuffer(key));
		// AsymmetricBlockCipher e = new RSAEngine();
		// e = new org.bouncycastle.crypto.encodings.PKCS1Encoding(e);
		// e.init(true, publicKey);
		//
		// String inputdata = readFileAsString(inputFilename);
		// byte[] messageBytes = inputdata.getBytes();
		// int i = 0;
		// int len = e.getInputBlockSize();
		// while (i < messageBytes.length)
		// {
		// if (i + len > messageBytes.length)
		// len = messageBytes.length - i;
		//
		// byte[] hexEncodedCipher = e.processBlock(messageBytes, i, len);
		// value = value + getHexString(hexEncodedCipher);
		// i += e.getInputBlockSize();
		// }
		//
		// System.out.println(value);
		// BufferedWriter out = new BufferedWriter(new FileWriter(encryptedFilename));
		// out.write(value);
		// out.close();

		RSAEngine rsaEngine = new RSAEngine();
		AsymmetricBlockCipher cipher = new PKCS1Encoding(rsaEngine);

		cipher.init(true, key);

		int position = 0;
		int inputBlockSize = cipher.getInputBlockSize();
		byte[] result = new byte[0];
		while (position < data.length) {
			if (position + inputBlockSize > data.length)
				inputBlockSize = data.length - position;

			byte[] hexEncodedCipher = cipher.processBlock(data, position, inputBlockSize);
			result = combine(result, hexEncodedCipher);
			position += cipher.getInputBlockSize();
		}
		return result;
	}

	public static String toHex(byte[] data) {

		StringBuffer buf = new StringBuffer();

		for (int i = 0; i != data.length; i++) {
			int v = data[i] & 0xff;

			buf.append(digits.charAt(v >> 4));
			buf.append(digits.charAt(v & 0xf));
		}

		return buf.toString();
	}

	public static void installBCProvider() {
		if (Security.getProvider(BC) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {

		byte[] outBuffer = new byte[cipher.getOutputSize(data.length)];

		int length1 = cipher.processBytes(data, 0, data.length, outBuffer, 0);
		int length2 = cipher.doFinal(outBuffer, length1);

		byte[] result = new byte[length1 + length2];
		System.arraycopy(outBuffer, 0, result, 0, result.length);
		return result;
	}

	private static byte[] combine(byte[] one, byte[] two) {

		byte[] combined = new byte[one.length + two.length];

		System.arraycopy(one, 0, combined, 0, one.length);
		System.arraycopy(two, 0, combined, one.length, two.length);

		return combined;
	}
}
