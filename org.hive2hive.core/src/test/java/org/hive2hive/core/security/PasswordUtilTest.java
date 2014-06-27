package org.hive2hive.core.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import javax.crypto.SecretKey;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.security.EncryptionUtil.AES_KEYLENGTH;
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
			salt[i] = PasswordUtil.generateRandomSalt();
			assertNotNull(salt[i]);
			assertTrue(salt[i].length == PasswordUtil.SALT_BIT_SIZE / 8);

			logger.debug("Generated Salt: {}.", EncryptionUtil.byteToHex(salt[i]));

			// test whether salts are random
			for (int j = 0; j < i; j++) {
				assertFalse(Arrays.equals(salt[i], salt[j]));
			}
		}
	}

	@Test
	public void generateFixedSaltTest() {

		// test for different input
		byte[][] input = new byte[5][];
		for (int i = 0; i < input.length; i++) {
			input[i] = generateRandomString(15).getBytes();

			logger.debug("Random Input: {}.", EncryptionUtil.byteToHex(input[i]));

			byte[][] fixedSalt = new byte[10][];
			for (int j = 0; j < fixedSalt.length; j++) {

				// test fixed salt generation
				fixedSalt[j] = PasswordUtil.generateFixedSalt(input[i]);

				assertNotNull(fixedSalt[j]);
				assertTrue(fixedSalt[j].length == PasswordUtil.SALT_BIT_SIZE / 8);

				logger.debug("Generated Fixed Salt: {}.", EncryptionUtil.byteToHex(fixedSalt[j]));

				// test whether salts are equal
				for (int k = 0; k < j; k++) {
					assertTrue(Arrays.equals(fixedSalt[k], fixedSalt[j]));
				}
			}
		}
	}

	@Test
	public void generateAESKeyFromPasswordTest() {

		// test all key sizes
		AES_KEYLENGTH[] sizes = EncryptionUtilTest.getAESKeySizes();

		for (int s = 0; s < sizes.length; s++) {

			// test various UserPasswords
			for (int i = 0; i < 3; i++) {

				String randomPW = generateRandomString(20);
				String randomPIN = generateRandomString(6);

				logger.debug("Testing {}-bit AES key generation from user password and PIN:", sizes[s].value());
				logger.debug("Random PW: {}.", randomPW);
				logger.debug("Random PIN: {}.", randomPIN);

				// test the generation process multiple times to ensure consistent result
				SecretKey[] aesKey = new SecretKey[3];
				for (int j = 0; j < aesKey.length; j++) {

					// generate AES key
					aesKey[j] = PasswordUtil.generateAESKeyFromPassword(randomPW, randomPIN, sizes[s]);

					assertNotNull(aesKey[j]);
					assertNotNull(aesKey[j].getEncoded());
					assertTrue(aesKey[j].getEncoded().length == sizes[s].value() / 8);

					logger.debug("Generated {}-bit AES key: {}.", sizes[s].value(),
							EncryptionUtil.byteToHex(aesKey[j].getEncoded()));

					// test whether generated AES passwords are equal
					for (int k = 0; k < j; k++) {
						assertTrue(Arrays.equals(aesKey[k].getEncoded(), aesKey[j].getEncoded()));
					}
				}
			}

		}
	}

	@Test
	public void generateHashTest() {

		// test various passwords
		char[][] password = new char[5][];
		for (int i = 0; i < password.length; i++) {

			// set a random password and salt
			password[i] = generateRandomString(20).toCharArray();
			byte[] salt = PasswordUtil.generateRandomSalt();

			logger.debug("Tested Password: {}.", String.valueOf(password[i]));

			// test hash generation
			byte[] hash = PasswordUtil.generateHash(password[i], salt);

			assertNotNull(hash);
			assertTrue(hash.length == PasswordUtil.HASH_BIT_SIZE / 8);

			logger.debug("Generated Salt: {}.", EncryptionUtil.byteToHex(hash));

			// test if hash outcome stays always the same with the same password and salt
			for (int j = 0; j < 10; j++) {
				assertTrue(Arrays.equals(hash, PasswordUtil.generateHash(password[i], salt)));
			}

			// test if hash outcome changes with other password or salt
			for (int j = 0; j < 10; j++) {

				// assure new parameters
				char[] otherPW;
				do {
					otherPW = generateRandomString(20).toCharArray();
				} while (Arrays.equals(otherPW, password[i]));
				byte[] otherSalt;
				do {
					otherSalt = PasswordUtil.generateRandomSalt();
				} while (Arrays.equals(otherSalt, salt));

				assertFalse(Arrays.equals(hash, PasswordUtil.generateHash(password[i], otherSalt)));
				assertFalse(Arrays.equals(hash, PasswordUtil.generateHash(otherPW, salt)));
				assertFalse(Arrays.equals(hash, PasswordUtil.generateHash(otherPW, otherSalt)));
			}
		}
	}

	@Test
	public void validatePasswordTest() {

		// test various passwords
		char[][] password = new char[20][];
		for (int i = 0; i < password.length; i++) {

			// set a random password and salt
			password[i] = generateRandomString(20).toCharArray();
			byte[] salt = PasswordUtil.generateRandomSalt();

			logger.debug("Validating password '{}' with salt '{}'.", String.valueOf(password[i]),
					EncryptionUtil.byteToHex(salt));

			// generate hash
			byte[] hash = PasswordUtil.generateHash(password[i], salt);

			// validate password
			boolean isValid = PasswordUtil.validatePassword(password[i], salt, hash);

			assertTrue(isValid);

			// test validation with wrong password, salt or hash
			for (int j = 0; j < 3; j++) {

				// assure new parameters
				char[] otherPW;
				do {
					otherPW = generateRandomString(20).toCharArray();
				} while (Arrays.equals(otherPW, password[i]));
				byte[] otherSalt;
				do {
					otherSalt = PasswordUtil.generateRandomSalt();
				} while (Arrays.equals(otherSalt, salt));
				byte[] otherHash = null;
				do {
					otherHash = PasswordUtil.generateHash(generateRandomString(20).toCharArray(),
							PasswordUtil.generateRandomSalt());
				} while (Arrays.equals(otherHash, hash));

				assertFalse(PasswordUtil.validatePassword(otherPW, salt, hash));
				assertFalse(PasswordUtil.validatePassword(password[i], otherSalt, hash));
				assertFalse(PasswordUtil.validatePassword(password[i], salt, otherHash));

				assertFalse(PasswordUtil.validatePassword(otherPW, otherSalt, hash));
				assertFalse(PasswordUtil.validatePassword(password[i], otherSalt, otherHash));
				assertFalse(PasswordUtil.validatePassword(otherPW, salt, otherHash));

				assertFalse(PasswordUtil.validatePassword(otherPW, otherSalt, otherHash));
			}
		}
	}
}
