package org.hive2hive.core.test.encryption;

import static org.junit.Assert.*;

import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.hive2hive.core.encryption.EncryptionUtil;
import org.hive2hive.core.encryption.PasswordUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class PasswordUtilTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = PasswordUtilTest.class;
		beforeClass();
	}

	@Test
	public void generateSaltTest() {

		byte[][] salt = new byte[100][];
		for (int i = 0; i < salt.length; i++) {

			// test salt generation
			salt[i] = PasswordUtil.generateSalt();
			assertNotNull(salt[i]);
			assertTrue(salt[i].length == PasswordUtil.SALT_BIT_SIZE);

			logger.debug(String.format("Generated Salt: %s", EncryptionUtil.toHex(salt[i])));

			// test whether salts are random
			for (int j = 0; j < i; j++) {
				assertFalse(Arrays.equals(salt[i], salt[j]));
			}
		}
	}

	@Test
	public void generateHashTest() {

		// test various passwords
		char[][] password = new char[5][];
		for (int i = 0; i < password.length; i++) {

			// set a random password and salt
			password[i] = generateRandomString().toCharArray();
			byte[] salt = PasswordUtil.generateSalt();

			logger.debug(String.format("Tested Password: %s", String.valueOf(password[i])));

			// test hash generation
			byte[] hash = null;
			try {
				hash = PasswordUtil.generateHash(password[i], salt);
			} catch (InvalidKeySpecException e) {
				logger.error("Exception while testing password hash generation.", e);
				e.printStackTrace();
			}

			assertNotNull(hash);
			assertTrue(hash.length == PasswordUtil.HASH_BIT_SIZE / 8);

			logger.debug(String.format("Generated Salt: %s", EncryptionUtil.toHex(hash)));

			// test if hash outcome stays always the same with the same password and salt
			for (int j = 0; j < 10; j++) {
				try {
					assertTrue(Arrays.equals(hash, PasswordUtil.generateHash(password[i], salt)));
				} catch (InvalidKeySpecException e) {
					logger.error("Exception while testing password hash generation.", e);
					e.printStackTrace();
				}
			}

			// test if hash outcome changes with other password or salt
			for (int j = 0; j < 10; j++) {
				
				// assure new parameters
				char[] otherPW;
				do {
					otherPW = generateRandomString().toCharArray();
				} while (Arrays.equals(otherPW, password[i]));
				byte[] otherSalt;
				do {
					otherSalt = PasswordUtil.generateSalt();
				} while (Arrays.equals(otherSalt, salt));

				try {
					assertFalse(Arrays.equals(hash, PasswordUtil.generateHash(password[i], otherSalt)));
					assertFalse(Arrays.equals(hash, PasswordUtil.generateHash(otherPW, salt)));
					assertFalse(Arrays.equals(hash, PasswordUtil.generateHash(otherPW, otherSalt)));
				} catch (InvalidKeySpecException e) {
					logger.error("Exception while testing password hash generation.", e);
					e.printStackTrace();
				}
			}
		}
	}

	@Test
	public void validatePasswordTest() {

		// test various passwords
		char[][] password = new char[20][];
		for (int i = 0; i < password.length; i++) {

			// set a random password and salt
			password[i] = generateRandomString().toCharArray();
			byte[] salt = PasswordUtil.generateSalt();

			logger.debug(String.format("Validating password %s", String.valueOf(password[i])));
			logger.debug(String.format("with salt %s", EncryptionUtil.toHex(salt)));
			
			// generate hash
			byte[] hash = null;
			try {
				hash = PasswordUtil.generateHash(password[i], salt);
			} catch (InvalidKeySpecException e) {
				logger.error("Exception whil testing password validation.", e);
				e.printStackTrace();
			}

			// validate password
			boolean isValid = false;
			try {
				isValid = PasswordUtil.validatePassword(password[i], salt, hash);
			} catch (InvalidKeySpecException e) {
				logger.error("Exception whil testing password validation.", e);
				e.printStackTrace();
			}

			assertTrue(isValid);

			// test validation with wrong password, salt or hash
			for (int j = 0; j < 10; j++) {
				
				// assure new parameters
				char[] otherPW;
				do {
					otherPW = generateRandomString().toCharArray();
				} while (Arrays.equals(otherPW, password[i]));
				byte[] otherSalt;
				do {
					otherSalt = PasswordUtil.generateSalt();
				} while (Arrays.equals(otherSalt, salt));
				byte[] otherHash = null;
				do {
					try {
						otherHash = PasswordUtil.generateHash(generateRandomString().toCharArray(), PasswordUtil.generateSalt());
					} catch (InvalidKeySpecException e) {
						logger.error("Exception whil testing password validation.", e);
						e.printStackTrace();
					}
				} while (Arrays.equals(otherHash, hash));
							
				try {
					assertFalse(PasswordUtil.validatePassword(otherPW, salt, hash));
					assertFalse(PasswordUtil.validatePassword(password[i], otherSalt, hash));
					assertFalse(PasswordUtil.validatePassword(password[i], salt, otherHash));
					
					assertFalse(PasswordUtil.validatePassword(otherPW, otherSalt, hash));
					assertFalse(PasswordUtil.validatePassword(password[i], otherSalt, otherHash));
					assertFalse(PasswordUtil.validatePassword(otherPW, salt, otherHash));
					
					assertFalse(PasswordUtil.validatePassword(otherPW, otherSalt, otherHash));
				} catch (InvalidKeySpecException e) {
					logger.error("Exception whil testing password validation.", e);
					e.printStackTrace();
				}
			}
		}
	}
}
