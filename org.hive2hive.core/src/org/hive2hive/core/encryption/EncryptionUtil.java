package org.hive2hive.core.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * This class provides fundamental encryption and decryption functionalities as well as key generation
 * methods.
 * 
 * @author Christian
 * 
 */
public final class EncryptionUtil {

	// TODO throw exceptions where the library should
	
	private static final H2HLogger logger = H2HLoggerFactory.getLogger(EncryptionUtil.class);

	private static final String AES_CIPHER_MODE = "AES/CBC/PKCS5PADDING";
	private static final String RSA_CIPHER_MODE = "RSA";

	private static final String MD5_SIGNATURE_ALGORITHM = "MD5WithRSA";

	private static final String ISO_8859_1 = "ISO-8859-1";

	public enum AES_KEYLENGTH {
		BIT_128(128);
		// BIT192 (192);
		// BIT256 (256);

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

	/**
	 * Symmetrically encrypts byte[] content by means of the AES algorithm.
	 * 
	 * @param content The content to be encrypted.
	 * @param aesKey The symmetric key with which the content will be encrypted.
	 * @return EncryptedContent which contains the encrypted byte[] content as well as the AES initialization
	 *         vector (IV).
	 */
	public static EncryptedContent encryptAES(byte[] content, SecretKey aesKey) {
		return encrypt(content, aesKey, AES_CIPHER_MODE);
	}

	/**
	 * Symmetrically decrypts a prior EncryptedContent by means of the AES algorithm.
	 * 
	 * @param content The EncryptedContent to be decrypted.
	 * @param aesKey The symmetric key with which the content will be decrypted.
	 * @return decrypted byte[] content
	 */
	public static byte[] decryptAES(EncryptedContent content, SecretKey aesKey) {
		return decrypt(content, aesKey, AES_CIPHER_MODE);
	}

	/**
	 * Asymmetrically encrypts byte[] content by means of the RSA algorithm. In order to encrypt the content,
	 * a public RSA key has to be provided.
	 * 
	 * @param content The content to be encrypted.
	 * @param publicKey The asymmetric public key with which the content will be encrypted.
	 * @return EncryptedContent which contains the encrypted byte[] content.
	 */
	public static EncryptedContent encryptRSA(byte[] content, PublicKey publicKey) {
		return encrypt(content, publicKey, RSA_CIPHER_MODE);
	}

	/**
	 * Asymmetrically decrypts a prior EncryptedContent by means of the RSA algorithm. In order to decrypt the
	 * content, a private RSA key has to be provided.
	 * NOTE: RSA can only encrypt data that has a maximum byte length of: ((key length in bits / 8) - 11)
	 * bytes. E.g. 256 bytes with a 2048 bits key.
	 * 
	 * @param content The EncryptedContent to be decrypted.
	 * @param privateKey The asymmetric private key with which the content will be decrypted.
	 * @return decrypted byte[] content.
	 */
	public static byte[] decryptRSA(EncryptedContent content, PrivateKey privateKey) {
		return decrypt(content, privateKey, RSA_CIPHER_MODE);
	}

	// public static CipherInputStream encryptStreamAES(InputStream inputStream, SecretKey aesKey) {
	// return encryptStream(inputStream, aesKey, AES_CIPHER_MODE);
	// }

	// public static CipherInputStream decryptStreamAES(InputStream inputStream, SecretKey aesKey) {
	// return decryptStream(inputStream, aesKey, AES_CIPHER_MODE);
	// }

	// public static CipherInputStream encryptStreamRSA(InputStream inputStream, PublicKey publicKey) {
	// return encryptStream(inputStream, publicKey, RSA_CIPHER_MODE);
	// }

	// public static CipherInputStream decryptStreamRSA(InputStream inputStream, PrivateKey privateKey) {
	// return decryptStream(inputStream, privateKey, RSA_CIPHER_MODE);
	// }

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
	 * Creates a symmetric AES key of the specified key length.
	 * 
	 * @param keyLength The length the AES key should have.
	 * @return A symmetric AES key of the specified length.
	 */
	public static SecretKey createAESKey(AES_KEYLENGTH keyLength) {

		try {
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(keyLength.value(), new SecureRandom());
			SecretKey skey = kg.generateKey();
			byte[] raw = skey.getEncoded();
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			return skeySpec;
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception while creating AES key:", e);
		}
		return null;
	}

	/**
	 * Creates an asymmetric RSA key pair of the specified key length.
	 * 
	 * @param keyLength The length the RSA keys should have.
	 * @return An asymmetric RSA key pair of the specified length.
	 */
	public static KeyPair createRSAKeys(RSA_KEYLENGTH keyLength) {

		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(keyLength.value(), new SecureRandom());
			return kpg.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception while creating RSA keys:", e);
		}
		return null;
	}

	public static SecretKey createAESKeyFromPassword(UserPassword password, AES_KEYLENGTH keyLength) {

		try {
			SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password.getPassword(), password.getSalt(), 65536,
					keyLength.value());
			SecretKey tmpKey = kf.generateSecret(spec);
			SecretKey key = new SecretKeySpec(tmpKey.getEncoded(), "AES");
			return key;
		} catch (NoSuchAlgorithmException | NullPointerException | IllegalArgumentException
				| InvalidKeySpecException e) {
			logger.error("Exception while creating AES key from password:", e);
		}
		return null;
	}

	/**
	 * Creates a random salt that can be used in combination with a key in order to prevent dictionary
	 * attacks.
	 * 
	 * @return A random 8 byte salt.
	 */
	public static byte[] createSalt(int byteLength) {

		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[byteLength];
		random.nextBytes(salt);
		return salt;
	}

	/**
	 * Signs the provided content with the specified private key.
	 * 
	 * @param content The content to be signed.
	 * @param privateKey The private key used to sign the content.
	 * @return A SignedContent object which is used to keep track of signature information of a signed
	 *         content, which will be used once the content needs to be verified.
	 */
	public static SignedContent sign(byte[] content, PrivateKey privateKey) {

		byte[] signatureBytes = null;

		try {
			Signature signature = Signature.getInstance(MD5_SIGNATURE_ALGORITHM);
			signature.initSign(privateKey);
			signature.update(content);
			signatureBytes = signature.sign();

		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			logger.error("Exception while signing:", e);
		}

		return new SignedContent(content, signatureBytes);
	}

	/**
	 * Verifies the signature of the provided content with the specified public key.
	 * @param content The content to be verified.
	 * @param publicKey The public key used to verify the content.
	 * @return Returns true if the signature could be verified and false otherwise.
	 */
	public static boolean verify(SignedContent content, PublicKey publicKey) {

		try {
			Signature signature = Signature.getInstance(MD5_SIGNATURE_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(content.getOriginalData());
			return signature.verify(content.getSignatureBytes());
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			logger.error("Exception while verifying:", e);
		}
		return false;
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

	private static byte[] decrypt(EncryptedContent content, Key key, String transformationMode) {

		byte[] decryptedContent = null;

		try {
			// declare transformation mode
			Cipher cipher = Cipher.getInstance(transformationMode);
			try {
				// initialize cipher with decryption mode, key (and initialization vector)
				switch (transformationMode) {
					case AES_CIPHER_MODE:
						IvParameterSpec ivSpec = new IvParameterSpec(content.getInitVector());
						cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
						break;
					case RSA_CIPHER_MODE:
						cipher.init(Cipher.DECRYPT_MODE, key);
						break;
				}
				try {
					// decrypt the content
					decryptedContent = cipher.doFinal(content.getCipherContent());
				} catch (IllegalBlockSizeException | BadPaddingException e) {
					logger.error("Exception during decryption:", e);
				}
			} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
				logger.error("Invalid key or algorithm parameter:", e);
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			logger.error("Exception during cipher initialization:", e);
		}

		return decryptedContent;
	}

	// private static CipherInputStream encryptStream(InputStream inputStream, Key key, String
	// transformationMode) {
	//
	// Cipher encryptionCipher = getEncryptionCipher(key, transformationMode);
	// return new CipherInputStream(inputStream, encryptionCipher);
	// }

	// private static CipherInputStream decryptStream(InputStream inputStream, Key key, String
	// transformationMode) {
	//
	// Cipher decryptionCipher = getDecryptionCipher(key, transformationMode);
	// return new CipherInputStream(inputStream, decryptionCipher);
	// }

	private static byte[] combine(byte[] arrayA, byte[] arrayB) {

		byte[] result = new byte[arrayA.length + arrayB.length];
		System.arraycopy(arrayA, 0, result, 0, arrayA.length);
		System.arraycopy(arrayB, 0, result, arrayA.length, arrayB.length);
		return result;
	}
}
