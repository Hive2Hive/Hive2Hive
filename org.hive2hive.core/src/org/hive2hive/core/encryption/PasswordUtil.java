package org.hive2hive.core.encryption;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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

	public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

	public static final int SALT_BYTE_SIZE = 24; // should be equal to HASH_BYTE_SIZE
	public static final int HASH_BYTE_SIZE = 24; // should be equal to SALT_BYTE_SIZE
	public static final int PBKDF2_ITERATIONS = 1000; // slowing factor

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
		byte[] salt = new byte[SALT_BYTE_SIZE];
		random.nextBytes(salt);
		return salt;
	}

	/**
	 * Returns a salted PBKDF2 hash of the password.
	 * 
	 * @param password the password to hash
	 * @return a salted PBKDF2 hash of the password
	 */
	public static byte[] generateHash(char[] password, byte[] salt) throws InvalidKeySpecException {

		// hash the password
		return pbkdf2(password, salt);
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
		byte[] testHash = pbkdf2(password, salt);

		// compare the hashes in constant time
		return slowCompare(correctHash, testHash);
	}

	/**
	 * Computes the PBKDF2 hash of a password.
	 * 
	 * @param password the password to hash
	 * @param salt the salt
	 * @param bytes the length of the hash to compute in bytes
	 * @return the PBDKF2 hash of the password
	 */
	private static byte[] pbkdf2(char[] password, byte[] salt) throws InvalidKeySpecException {

		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);

			PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE * 8);
			return skf.generateSecret(spec).getEncoded();

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
