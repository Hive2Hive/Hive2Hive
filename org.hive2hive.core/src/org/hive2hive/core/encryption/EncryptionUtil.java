package org.hive2hive.core.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

import org.eclipse.emf.ecore.xml.type.internal.DataValue.Base64;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

public final class EncryptionUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(EncryptionUtil.class);

	private static final String AES_CIPHER_MODE = "AES/CBC/PKCS5PADDING";
	private static final String RSA_CIPHER_MODE = "RSA";

	private static final String ISO_8859_1 = "ISO-8859-1";
	private static final String UTF_8 = "UTF-8";

	private EncryptionUtil() {
	}

	private static EncryptedContent encrypt(byte[] content, Key key, String transformationMode) {

		byte[] encryptedContent = null;
		byte[] initVector = null;

		try {
			// declare transformation mode
			Cipher cipher = Cipher.getInstance(transformationMode);
			try {
				// initialize cipher with encryption mode and key
				cipher.init(Cipher.ENCRYPT_MODE, key);
				try {
					// encrypt the content
					encryptedContent = cipher.doFinal(content);
					initVector = cipher.getIV();
				} catch (IllegalBlockSizeException | BadPaddingException e) {
					logger.error("Exception during encryption:", e);
				}
			} catch (InvalidKeyException e) {
				logger.error("Invalid key:", e);
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Exception during cipher initialization:", e);
		}

		return new EncryptedContent(encryptedContent, initVector);
	}

	public static EncryptedContent encryptAES(byte[] content, SecretKey aesKey) {
		return encrypt(content, aesKey, AES_CIPHER_MODE);
	}

	public static EncryptedContent encryptRSA(byte[] content, PublicKey publicKey) {
		return encrypt(content, publicKey, RSA_CIPHER_MODE);
	}

	private static byte[] decrypt(EncryptedContent content, Key key, String transformationMode) {

		byte[] decryptedContent = null;

		try {
			// declare transformation mode
			Cipher cipher = Cipher.getInstance(transformationMode);
			try {
				// initialize cipher with decryption mode, key and initialization vector
				cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(content.getInitVector()));
				try {
					// decrypt the content
					decryptedContent = cipher.doFinal(content.getContent());
				} catch (IllegalBlockSizeException | BadPaddingException e) {
					logger.error("Exception during decryption:", e);
				}
			} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
				logger.error("Invalid key or parameter:", e);
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Exception during cipher initialization:", e);
		}

		return decryptedContent;
	}

	public static byte[] decryptAES(EncryptedContent content, SecretKey aesKey) {
		return decrypt(content, aesKey, AES_CIPHER_MODE);
	}

	public static byte[] decryptRSA(EncryptedContent content, PrivateKey privateKey) {
		return decrypt(content, privateKey, RSA_CIPHER_MODE);
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

	public static byte[] createRandomAESKey() {

		SecureRandom random = new SecureRandom();
		byte[] aesKey = new byte[16]; // 16 bytes = 128 bits
		random.nextBytes(aesKey);
		return aesKey;
	}

	public static SecretKey createSecretAESKey() {
		KeyGenerator keyGenerator;
		try {
			keyGenerator = KeyGenerator.getInstance("AES");
			// For more security use http://www.bouncycastle.org/
			keyGenerator.init(128);
			SecretKey key = keyGenerator.generateKey();
			return key;
		} catch (NoSuchAlgorithmException e) {
			logger.error("Error during key generation:", e);
		}
		return null;
	}

	public static KeyPair createRSAKeys() {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception during key creation:", e);
		}
		return null;
	}

	private static SecretKey createDESKey(String aPassword, byte[] someSalt) {

		byte[] tempKey = combine(toByte(aPassword), someSalt);

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e1) {
			logger.error("Exception  during digest creation:", e1);
			return null;
		}
		for (int i = 0; i < 1024; i++) {
			md.update(tempKey);
			tempKey = md.digest();
			tempKey = combine(someSalt, tempKey);
		}

		try {
			DESKeySpec dks = new DESKeySpec(tempKey);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			SecretKey desKey = skf.generateSecret(dks);
			return desKey;
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Exception during des key creation from password:", e);
		}
		return null;
	}

	public static String encryptPrivateKey(PrivateKey aPrivateKey, byte[] anEasKey) {
		return convertKeyToString(aPrivateKey, false);
	}

//	public static PrivateKey decryptPrivateKey(String aString, byte[] anEasKey) {
//		return (PrivateKey) convertStringToKey(aString, false);
//	}

	public static String convertKeyToString(Key aKey, boolean isPublic) {
		KeyFactory fact;
		try {
			BigInteger m, e;
			fact = KeyFactory.getInstance("RSA");
			if (isPublic) {
				RSAPublicKeySpec publicKeySpec = fact.getKeySpec(aKey, RSAPublicKeySpec.class);
				m = publicKeySpec.getModulus();
				e = publicKeySpec.getPublicExponent();
			} else {
				RSAPrivateKeySpec privateKeySpec = fact.getKeySpec(aKey, RSAPrivateKeySpec.class);
				m = privateKeySpec.getModulus();
				e = privateKeySpec.getPrivateExponent();
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append(Base64.encode(m.toByteArray()));
		//	buffer.append(DELIMITER);
			buffer.append(Base64.encode(e.toByteArray()));
			return buffer.toString();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Exception while extracting the information of the key:", e);
		}
		return null;
	}

//	public static Key convertStringToKey(String aString, boolean isPublic) {
//		String[] parts = aString.split(DELIMITER);
//		BigInteger m = new BigInteger(Base64.decode(parts[0]));
//		BigInteger e = new BigInteger(Base64.decode(parts[1]));
//		try {
//			KeyFactory fact = KeyFactory.getInstance("RSA");
//			if (isPublic) {
//				RSAPublicKeySpec pbuclicKeySpec = new RSAPublicKeySpec(m, e);
//				return fact.generatePublic(pbuclicKeySpec);
//			} else {
//				RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(m, e);
//				return fact.generatePrivate(privateKeySpec);
//			}
//		} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
//			logger.error("Exception while creating the key:", e1);
//		}
//		return null;
//	}

	// public static Key convertStringToKey(String aString, boolean isPublic) {
	// String[] parts = aString.split(DELIMITER);
	// BigInteger m = new BigInteger(Base64.decode(parts[0]));
	// BigInteger e = new BigInteger(Base64.decode(parts[1]));
	// try {
	// KeyFactory fact = KeyFactory.getInstance("RSA");
	// if (isPublic) {
	// RSAPublicKeySpec pbuclicKeySpec = new RSAPublicKeySpec(m, e);
	// return fact.generatePublic(pbuclicKeySpec);
	// } else {
	// RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(m, e);
	// return fact.generatePrivate(privateKeySpec);
	// }
	// } catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
	// logger.error("Exception while creating the key:", e1);
	// }
	// return null;
	// }

	private static String encryptTempKey(byte[] aesKey, String aPassword, byte[] someSalt) {
		try {
			SecretKey desKey = createDESKey(aPassword, someSalt);
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, desKey);
			return toString(cipher.doFinal(aesKey));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Exception during temp key encryption:", e);
		}
		return null;
	}

	private static byte[] decryptTempKey(String aKeyAsString, String aPassword, String someSalt) {
		try {
			SecretKey desKey = createDESKey(aPassword, toByte(someSalt));
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, desKey);
			return cipher.doFinal(toByte(aKeyAsString));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| IllegalBlockSizeException | BadPaddingException e) {
			logger.error("Exception during temp key decryption:", e);
		}
		return null;
	}

	private static byte[] createRandomSalt() {
		Random r = new SecureRandom();
		byte[] salt = new byte[20];
		r.nextBytes(salt);
		return salt;
	}

	private static byte[] combine(byte[] arrayA, byte[] arrayB) {
		byte[] result = new byte[arrayA.length + arrayB.length];
		System.arraycopy(arrayA, 0, result, 0, arrayA.length);
		System.arraycopy(arrayB, 0, result, arrayA.length, arrayB.length);
		return result;
	}

}
