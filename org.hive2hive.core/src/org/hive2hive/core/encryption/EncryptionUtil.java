package org.hive2hive.core.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class EncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(EncryptionUtil.class);

	private static String digits = "0123456789abcdef";

	public enum AES_KEYLENGTH {
		BIT_128(128),
		BIT_192(192),
		BIT_256(256);

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
			final KeyGenerator kg = KeyGenerator.getInstance("AES", "BC");
			kg.init(keyLength.value(), new SecureRandom());
			byte[] encoded = kg.generateKey().getEncoded();
			return new SecretKeySpec(encoded, "AES");
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
		KeyGenerationParameters parameters = new RSAKeyGenerationParameters(publicExp, new SecureRandom(),
				strength, certainty);
		kpg.init(parameters);

		AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();
		return keyPair;
	}

	public static byte[] encryptAES(byte[] data, SecretKey secretKey, byte[] initVector)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {

		return processAESCiphering(true, data, secretKey, initVector);
	}

	public static byte[] decryptAES(byte[] data, SecretKey secretKey, byte[] initVector)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {

		return processAESCiphering(false, data, secretKey, initVector);
	}

	public static byte[] encryptRSA(byte[] data, CipherParameters publicKey)
			throws InvalidCipherTextException {

		return processRSACiphering(true, data, publicKey);
	}

	public static byte[] decryptRSA(byte[] data, CipherParameters privateKey)
			throws InvalidCipherTextException {

		return processRSACiphering(false, data, privateKey);
	}

	public static byte[] sign(byte[] data, CipherParameters privateKey) throws DataLengthException,
			CryptoException {

		return setupSigner(true, data, privateKey).generateSignature();
	}

	public static boolean verify(byte[] data, byte[] signature, CipherParameters publicKey) {

		return setupSigner(false, data, publicKey).verifySignature(signature);
	}

	public static byte[] serializeObject(Object object) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		byte[] result = null;

		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			result = baos.toByteArray();
		} catch (IOException e) {
			logger.error("Exception while serializing object.");
		} finally {
			try {
				oos.close();
				baos.close();
			} catch (IOException e) {
				logger.error("Exception while closing serialization process.");
			}
		}
		return result;
	}

	public static Object deserializeObject(byte[] bytes) {

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		Object result = null;

		try {
			ois = new ObjectInputStream(bais);
			result = ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			logger.error("Exception while deserializing object.");
		} finally {
			try {
				ois.close();
				bais.close();
			} catch (IOException e) {
				logger.error("Exception while closing deserialization process.");
			}
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

	private static void installBCProvider() {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	private static byte[] processAESCiphering(boolean forEncrypting, byte[] data, SecretKey key,
			byte[] initVector) throws DataLengthException, IllegalStateException, InvalidCipherTextException {

		// set up engine, block cipher mode and padding
		AESEngine aesEngine = new AESEngine();
		CBCBlockCipher cbc = new CBCBlockCipher(aesEngine);
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc);

		// apply parameters
		CipherParameters parameters = new ParametersWithIV(new KeyParameter(key.getEncoded()), initVector);
		cipher.init(forEncrypting, parameters);

		// process ciphering
		byte[] output = new byte[cipher.getOutputSize(data.length)];

		int bytesProcessed1 = cipher.processBytes(data, 0, data.length, output, 0);
		int bytesProcessed2 = cipher.doFinal(output, bytesProcessed1);

		byte[] result = new byte[bytesProcessed1 + bytesProcessed2];
		System.arraycopy(output, 0, result, 0, result.length);
		return result;
	}

	private static byte[] processRSACiphering(boolean isEncrypting, byte[] data, CipherParameters key)
			throws InvalidCipherTextException {

		// set up engine and padding
		RSAEngine rsaEngine = new RSAEngine();
		AsymmetricBlockCipher cipher = new PKCS1Encoding(rsaEngine);

		// apply parameters
		cipher.init(isEncrypting, key);

		// process ciphering
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

	private static RSADigestSigner setupSigner(boolean forSigning, byte[] data, CipherParameters key) {
		
		// set up digester / hash function (e.g. SHA-1)
		SHA1Digest digester = new SHA1Digest();
		
		// set up signature mode (e.g. RSA)
		RSADigestSigner signer = new RSADigestSigner(digester);
		
		// apply parameters
		signer.init(forSigning, key);
		signer.update(data, 0, data.length);
		
		return signer;
	}

	private static byte[] combine(byte[] one, byte[] two) {

		byte[] combined = new byte[one.length + two.length];

		System.arraycopy(one, 0, combined, 0, one.length);
		System.arraycopy(two, 0, combined, one.length, two.length);

		return combined;
	}
}
