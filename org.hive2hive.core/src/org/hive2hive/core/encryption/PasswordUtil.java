package org.hive2hive.core.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.hive2hive.core.encryption.EncryptionUtil.AES_KEYLENGTH;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * This class provides basic functionalities regarding password validation.
 * 
 * @author Christian
 * 
 */
public final class PasswordUtil {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(PasswordUtil.class);

	public static final int HASH_BIT_SIZE = 192; 
	public static final int SALT_BIT_SIZE = HASH_BIT_SIZE;
	public static final int PBKDF2_ITERATIONS = 65536; // slowing factor

	private PasswordUtil() {
	}

	/**
	 * Generates a random salt that can be used in combination with a password in order to prevent
	 * dictionary and brute-force attacks.
	 * 
	 * @return A random salt.
	 */
	public static byte[] generateSalt() {
	
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[SALT_BIT_SIZE];
		random.nextBytes(salt);
		return salt;
	}

	/**
	 * Generates a user password based on the user defined password. This function randomly generates a salt
	 * that is attached to this password.
	 * 
	 * @param password
	 * @return Returns a UserPassword that holds the password and its associated salt.
	 */
	public static UserPassword generatePassword(char[] password) {
	
		return new UserPassword(password, generateSalt());
	}

	/**
	 * Returns a salted PBKDF2 hash of the password.
	 * 
	 * @param password the password to hash
	 * @return a salted PBKDF2 hash of the password
	 */
	public static byte[] generateHash(char[] password, byte[] salt) throws InvalidKeySpecException {
	
		// hash the password
		return getPBKDF2Hash(password, salt, HASH_BIT_SIZE);
	}

	/**
	 * Generates a symmetric AES key of the specified size and based on the provided UserPassword.
	 * @param upw The UserPassword from which the AES key is derivated.
	 * @param keyLength The desired key length of the resulting AES key.
	 * @return Returns the derived symmetric AES key of desired size.
	 * @throws InvalidKeySpecException
	 */
	public static SecretKey generateAESKeyFromPassword(UserPassword upw, AES_KEYLENGTH keyLength) throws InvalidKeySpecException {
		
		byte[] secretKeyEncoded = getPBKDF2Hash(upw.getPassword(), upw.getSalt(), keyLength.value());
		
		return new SecretKeySpec(secretKeyEncoded, "AES");
	}

	/**
	 * Validates a password using a hash.
	 * 
	 * @param password the password to check
	 * @param salt the salt
	 * @param correctHash the hash of the valid password
	 * @return true if the password is correct, false if not
	 */
	public static boolean validatePassword(char[] password, byte[] salt, byte[] correctHash)
			throws InvalidKeySpecException {

		// compute hash of password using same salt, iteration count and hash length
		byte[] testHash = getPBKDF2Hash(password, salt, HASH_BIT_SIZE);

		// compare the hashes in constant time
		return slowCompare(correctHash, testHash);
	}

	/**
	 * Computes the PBKDF2 hash of a password. This includes key size stretching where short passwords are enlarged up to the provided hash size.
	 * 
	 * @param password the password to hash
	 * @param salt the salt
	 * @param hashBitSize the length the hash should have (key size stretching)
	 * @param bytes the length of the hash to compute in bytes
	 * @return the PBDKF2 hash of the password
	 */
	private static byte[] getPBKDF2Hash(char[] password, byte[] salt, int hashBitSize) throws InvalidKeySpecException {

		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

			KeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, hashBitSize);
			SecretKey secretKey = skf.generateSecret(spec);
			return secretKey.getEncoded();

		} catch (NoSuchAlgorithmException e) {
			logger.error("Error while PBKDF2 key streching:", e);
		}
		return null;
	}

	/**
	 * Compares two byte arrays in length-constant time. This comparison method
	 * is used so that password hashes cannot be extracted from an on-line
	 * system using a timing attack and then attacked off-line.
	 * 
	 * @param a the first byte array
	 * @param b the second byte array
	 * @return true if both byte arrays are the same, false if not
	 */
	private static boolean slowCompare(byte[] a, byte[] b) {

		int diff = a.length ^ b.length;
		for (int i = 0; i < a.length && i < b.length; i++)
			diff |= a[i] ^ b[i];
		return diff == 0;
	}
}
