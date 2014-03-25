package org.hive2hive.core.test.security;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SignatureException;

import net.tomp2p.connection.SignatureFactory;
import net.tomp2p.message.SHA1Signature;
import net.tomp2p.storage.Data;

import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.core.security.H2HEncryptionUtil;
import org.hive2hive.core.security.H2HSignatureFactory;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class H2HSignatureFactoryTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = H2HSignatureFactoryTest.class;
		beforeClass();
	}

	@Test
	public void testSignature() throws InvalidKeyException, SignatureException, IOException {
		SignatureFactory signatureFactory = new H2HSignatureFactory();
		
		// generate some test data
		Data testData = new Data("test");
		// create a content protection key
		KeyPair protectionKey = H2HEncryptionUtil.generateProtectionKeys();
		
		SHA1Signature signature = signatureFactory.sign(protectionKey.getPrivate(), testData.buffer());
		
		boolean isVerified = signatureFactory.verify(protectionKey.getPublic(), testData.buffer(), signature);
		
		Assert.assertTrue(isVerified);
	}
	
	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
