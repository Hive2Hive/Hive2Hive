package org.hive2hive.core.test.encryption;

import org.hive2hive.core.encryption.JavaEncryptionUtil;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.BeforeClass;

public class JavaEncryptionUtilTest extends H2HJUnitTest {

	// TODO test exception handling in case wrong encryption keys are used
	JavaEncryptionUtil encryptionUtil = new JavaEncryptionUtil();
	
	@BeforeClass
	public static void initTest() throws Exception {
		testClass = JavaEncryptionUtilTest.class;
		beforeClass();
	}

//	@Test
//	public void createAESKeyFromPasswordTest() {
//
//		// TODO test whether always the same key is created from the password
//		Random random = new Random();
//
//		// check all key sizes
//		AES_KEYLENGTH[] sizes = getAESKeySizes();
//		for (int s = 0; s < sizes.length; s++) {
//
//			UserPassword[] pws = new UserPassword[50];
//			logger.debug(String.format("Testing %s AES keys from password of length %s bits.", pws.length,
//					sizes[s].value()));
//
//			for (int i = 0; i < pws.length; i++) {
//
//				// generate user passwords
//				pws[i] = ProfileEncryptionUtil.createUserPassword(new BigInteger(130, random).toString(32));
//
//				// generate AES key from password
//				SecretKey aesKey = encryptionUtil.createAESKeyFromPassword(pws[i], sizes[s]);
//
//				// check key
//				Assert.assertNotNull(aesKey);
//
//				// check key length
//				Assert.assertTrue(encryptionUtil.toString(aesKey.getEncoded()).length() == sizes[s].value() / 8);
//			}
//		}
//	}

//	@Test
//	public void encryptionAESWithPassword() {
//
//		// check all key sizes
//		AES_KEYLENGTH[] sizes = getAESKeySizes();
//
//		for (int s = 0; s < sizes.length; s++) {
//
//			// generate random sized content (max. 5MB)
//			SecureRandom random = new SecureRandom();
//			byte[] content = new byte[random.nextInt(5242880)];
//			random.nextBytes(content);
//
//			// generate user password
//			UserPassword upw = ProfileEncryptionUtil.createUserPassword("thisIsAPassword_&123");
//
//			logger.debug(String.format(
//					"Testing AES encryption with password of a sample %s byte file with a %s bit key.",
//					content.length, sizes[s].value()));
//
//			// generate AES key from password
//			SecretKey aesKey = encryptionUtil.createAESKeyFromPassword(upw, sizes[s]);
//
//			// encrypt content
//			EncryptedContent encryptedContent = encryptionUtil.encryptAES(content, aesKey);
//
//			Assert.assertNotNull(encryptedContent);
//			Assert.assertNotNull(encryptedContent.getCipherContent());
//			Assert.assertNotNull(encryptedContent.getInitVector());
//
//			Assert.assertFalse(Arrays.equals(encryptedContent.getCipherContent(), content));
//
//			// decrypt content
//			byte[] decryptedContent = encryptionUtil.decryptAES(encryptedContent, aesKey);
//
//			Assert.assertNotNull(decryptedContent);
//			Assert.assertTrue(Arrays.equals(content, decryptedContent));
//		}
//	}
}
