package org.hive2hive.core.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides basic functionalities regarding password validation.
 * 
 * @author Christian
 * 
 */
public final class PasswordUtil {

	private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);

	public static final int HASH_BIT_SIZE = 192;
	public static final int SALT_BIT_SIZE = HASH_BIT_SIZE;

	// slowing factor
	private static final int PBKDF2_ITERATIONS = 65536;

	private PasswordUtil() {
	}

	/**
	 * Generates a random salt that can be used in combination with a password in order to prevent
	 * dictionary and brute-force attacks.
	 * 
	 * @return A random salt.
	 */
	public static byte[] generateRandomSalt() {

		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[SALT_BIT_SIZE / 8];
		random.nextBytes(salt);
		return salt;
	}

	/**
	 * Generates a fixed salt based on the provided input. This means that always the same salt produced and
	 * returned.
	 * 
	 * @param input The input on which the fixed salt generation shall be based.
	 * @return Returns a fix salt.
	 */
	public static byte[] generateFixedSalt(byte[] input) {
		try {
			byte[] fixedSalt = new byte[SALT_BIT_SIZE / 8];

			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] state = sha.digest(input);
			sha.update(state);

			int offset = 0;
			while (offset < fixedSalt.length) {
				state = sha.digest();

				if (fixedSalt.length - offset > state.length) {
					System.arraycopy(state, 0, fixedSalt, offset, state.length);
				} else {
					System.arraycopy(state, 0, fixedSalt, offset, fixedSalt.length - offset);
				}

				offset += state.length;

				sha.update(state);
			}

			return fixedSalt;

		} catch (NoSuchAlgorithmException e) {
			logger.error("Exception while generating fixed salt:", e);
		}
		return null;
	}

	/**
	 * Returns a salted PBKDF2 hash of the password.
	 * 
	 * @param password the password to hash
	 * @return a salted PBKDF2 hash of the password
	 */
	public static byte[] generateHash(char[] password, byte[] salt) {
		// hash the password
		return getPBKDF2Hash(password, salt, HASH_BIT_SIZE);
	}

	/**
	 * Generates a symmetric AES key of the specified size and based on the provided UserPassword.
	 * 
	 * @param upw The UserPassword from which the AES key is derivated.
	 * @param keyLength The desired key lengt<h of the resulting AES key.
	 * @return Returns the derived symmetric AES key of desired size.
	 * @throws InvalidKeySpecException
	 */
	public static SecretKey generateAESKeyFromPassword(String password, String pin, AES_KEYLENGTH keyLength) {

		// generate a fixed salt out of the PIN itself
		byte[] pinEnlargementSalt = generateFixedSalt(pin.getBytes());

		// enlarge PIN with enlargement salt, such that PIN has same size as the hash
		byte[] enlargedPin = getPBKDF2Hash(pin.toCharArray(), pinEnlargementSalt, SALT_BIT_SIZE);

		// use the enlarged PIN as salt to generate the symmetric AES key
		byte[] secretKeyEncoded = getPBKDF2Hash(password.toCharArray(), enlargedPin, keyLength.value());

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
	public static boolean validatePassword(char[] password, byte[] salt, byte[] correctHash) {

		// compute hash of password using same salt, iteration count and hash length
		byte[] testHash = getPBKDF2Hash(password, salt, HASH_BIT_SIZE);

		// compare the hashes in constant time
		return slowCompare(correctHash, testHash);
	}

	/**
	 * Computes the PBKDF2 hash of a password. This includes key size stretching where short passwords are
	 * enlarged up to the provided hash size.
	 * 
	 * @param password the password to hash
	 * @param salt the salt
	 * @param hashBitSize the length the hash should have (key size stretching)
	 * @param bytes the length of the hash to compute in bytes
	 * @return the PBDKF2 hash of the password
	 */
	private static byte[] getPBKDF2Hash(char[] password, byte[] salt, int hashBitSize) {

		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

			KeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, hashBitSize);
			SecretKey secretKey = skf.generateSecret(spec);
			return secretKey.getEncoded();

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
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
		for (int i = 0; i < a.length && i < b.length; i++) {
			diff |= a[i] ^ b[i];
		}
		return diff == 0;
	}
}
