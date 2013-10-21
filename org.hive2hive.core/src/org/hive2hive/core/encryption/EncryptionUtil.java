package org.hive2hive.core.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class EncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(EncryptionUtil.class);

	private static final String AES_CIPHER_MODE = "AES/CBC/PKCS5PADDING";
	private static final String RSA_CIPHER_MODE = "RSA";

	private static final String ISO_8859_1 = "ISO-8859-1";

	private EncryptionUtil() {
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

	public static Object deserializeObject(byte[] object) {
	
		ByteArrayInputStream bais = new ByteArrayInputStream(object);
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

	public static EncryptedContent encryptAES(byte[] content, SecretKey aesKey) {
		return encrypt(content, aesKey, AES_CIPHER_MODE);
	}

	public static byte[] decryptAES(EncryptedContent content, SecretKey aesKey) {
		return decrypt(content, aesKey, AES_CIPHER_MODE);
	}

	public static EncryptedContent encryptRSA(byte[] content, PublicKey publicKey) {
		return encrypt(content, publicKey, RSA_CIPHER_MODE);
	}

	public static byte[] decryptRSA(EncryptedContent content, PrivateKey privateKey) {
		return decrypt(content, privateKey, RSA_CIPHER_MODE);
	}
	
	public static CipherInputStream encryptStreamAES(InputStream inputStream, SecretKey aesKey){
		return encryptStream(inputStream, aesKey, AES_CIPHER_MODE);
	}
	
	public static CipherOutputStream decryptStreamAES(OutputStream outputStream, SecretKey aesKey){
		return decryptStream(outputStream, aesKey, AES_CIPHER_MODE);
	}
	
	public static CipherInputStream encryptStreamRSA(InputStream inputStream, PublicKey publicKey){
		return encryptStream(inputStream, publicKey, RSA_CIPHER_MODE);
	}
	
	public static CipherOutputStream decryptStreamRSA(OutputStream outputStream, PrivateKey privateKey){
		return decryptStream(outputStream, privateKey, RSA_CIPHER_MODE);
	}
	
	public static byte[] createRandomAESKey() {
	
		SecureRandom random = new SecureRandom();
		byte[] aesKey = new byte[16]; // 16 bytes = 128 bits
		random.nextBytes(aesKey);
		return aesKey;
	}

	public static SecretKey createSecretAESKey() {
	
		// TODO For more security use http://www.bouncycastle.org/
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
			keyGenerator.init(128);
			return keyGenerator.generateKey();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception while secret AES key creation:", e);
		}
		return null;
	}

	public static KeyPair createRSAKeys() {
	
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception while RSA keys creation:", e);
		}
		return null;
	}

	public static SecretKey createDESKey(String password, byte[] salt) {
	
		byte[] tempKey = combine(toByte(password), salt);
	
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-512");
			
			for (int i = 0; i < 1024; i++) {
				messageDigest.update(tempKey);
				tempKey = messageDigest.digest();
				tempKey = combine(salt, tempKey);
			}
	
			try {
				DESKeySpec dks = new DESKeySpec(tempKey);
				SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
				return skf.generateSecret(dks);
			} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
				logger.error("Exception while DES key creation:", e);
			}	
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception  while DES digest creation:", e);
		}
		
		return null;
	}

	/**
	 * Converts a String to a byte array using the ISO-8859-1 char set.
	 * 
	 * @param string The String to convert.
	 * @return The byte array conversion result or null if the conversion fails.
	 */
	public static byte[] toByte(String string) {

		byte[] result = null;

		try {
			result = string.getBytes(ISO_8859_1);
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception while converting String to byte[]:", e);
		}
		return result;
	}

	/**
	 * Converts a byte array to a String using the ISO-8859-1 char set.
	 * 
	 * @param bytes The bytes to convert.
	 * @return The String conversion result or null if the conversion fails.
	 */
	public static String toString(byte[] bytes) {

		String result = null;

		try {
			result = new String(bytes, ISO_8859_1);
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception while converting byte[] to String:", e);
		}
		return result;
	}

	public static byte[] createRandomSalt() {
		
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[20];
		random.nextBytes(salt);
		return salt;
	}

	private static EncryptedContent encrypt(byte[] content, Key key, String transformationMode) {
	
		byte[] encryptedContent = null;
		byte[] initVector = null;
	
		Cipher encryptionCipher = getEncryptionCipher(key, transformationMode);
		try {
			// encrypt the content
			encryptedContent = encryptionCipher.doFinal(content);
			initVector = encryptionCipher.getIV();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Exception during encryption:", e);
		}
		
		return new EncryptedContent(encryptedContent, initVector);
	}

	private static byte[] decrypt(EncryptedContent content, Key key, String transformationMode) {
	
		byte[] decryptedContent = null;
	
		Cipher decryptionCipher = getDecryptionCipher(key, transformationMode);
		try {
			// decrypt the content
			decryptedContent = decryptionCipher.doFinal(content.getContent());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Exception during decryption:", e);
		}
		
		return decryptedContent;
	}

	private static CipherInputStream encryptStream(InputStream inputStream, Key key, String transformationMode){
		
		Cipher encryptionCipher = getEncryptionCipher(key, transformationMode);
		return new CipherInputStream(inputStream, encryptionCipher);
	}

	private static CipherOutputStream decryptStream(OutputStream outputStream, Key key, String transformationMode){
	
		Cipher decryptionCipher = getDecryptionCipher(key, transformationMode);
		return new CipherOutputStream(outputStream, decryptionCipher);
	}

	private static Cipher getEncryptionCipher(Key key, String transformationMode){
		try {
			// declare transformation mode
			Cipher cipher = Cipher.getInstance(transformationMode);
			try {
				// initialize cipher with encryption mode and key
				cipher.init(Cipher.ENCRYPT_MODE, key);
				return cipher;

			} catch (InvalidKeyException e) {
				logger.error("Invalid key:", e);
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Exception during cipher initialization:", e);
		}
		return null;
	}
	
	private static Cipher getDecryptionCipher(Key key, String transformationMode){
		try {
			// declare transformation mode
			Cipher cipher = Cipher.getInstance(transformationMode);
			try {
				// initialize cipher with decryption mode, key and initialization vector
				cipher.init(Cipher.DECRYPT_MODE, key);
				return cipher;
			} catch (InvalidKeyException e) {
				logger.error("Invalid key:", e);
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Exception during cipher initialization:", e);
		}
		return null;
	}

	private static byte[] combine(byte[] arrayA, byte[] arrayB) {
		
		byte[] result = new byte[arrayA.length + arrayB.length];
		System.arraycopy(arrayA, 0, result, 0, arrayA.length);
		System.arraycopy(arrayB, 0, result, arrayA.length, arrayB.length);
		return result;
	}
}
