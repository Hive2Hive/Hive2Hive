package org.hive2hive.core.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class EncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(EncryptionUtil.class);

	private static String digits = "0123456789abcdef";

	private static final String BC = "BC";
	public static final String AES = "AES";
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
			kg.init(keyLength.value());
			byte[] encoded = kg.generateKey().getEncoded();
			return new SecretKeySpec(encoded, AES);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			logger.error("Exception while key generator instance creation:", e);
		}
		return null;
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
			throws DataLengthException, IllegalStateException, InvalidCipherTextException{
		
		AESEngine aesEngine = new AESEngine();
		CBCBlockCipher cbc = new CBCBlockCipher(aesEngine);
		PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc);
		
	    CipherParameters parameters = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), initVector);
	    cipher.init(false, parameters);
	    
	    return cipherData(cipher, data);
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
}
