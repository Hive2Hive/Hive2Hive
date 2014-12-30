package org.hive2hive.core.security;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.Signature;
import java.security.SignatureException;

import net.tomp2p.connection.SignatureFactory;
import net.tomp2p.message.SignatureCodec;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class H2HSignatureFactoryTest extends H2HJUnitTest {

	private static KeyPair protectionKey;
	private static Data testData;

	@BeforeClass
	public static void initTest() throws Exception {
		// create a content protection key
		protectionKey = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_PROTECTION);
		// generate some test data
		testData = new Data("test");

		testClass = H2HSignatureFactoryTest.class;
		beforeClass();
	}

	@Test
	public void testSignVerify() throws InvalidKeyException, SignatureException, IOException {
		SignatureFactory signatureFactory = new H2HSignatureFactory();

		// sign the data
		SignatureCodec signature = signatureFactory.sign(protectionKey.getPrivate(), testData.buffer());

		// verify the data with the signature
		Assert.assertTrue(signatureFactory.verify(protectionKey.getPublic(), testData.buffer(), signature));
	}

	@Test
	public void testUpdateSingle() throws InvalidKeyException, SignatureException, IOException {
		// sign the data
		SignatureCodec signatureCodec = new H2HSignatureFactory().sign(protectionKey.getPrivate(), testData.buffer());

		// update (already belongs to the verification)
		Signature signature = new H2HSignatureFactory().update(protectionKey.getPublic(), testData.toByteBuffers());

		// verify the data with the signature
		Assert.assertTrue(signature.verify(signatureCodec.encode()));
	}

	@Test
	public void testUpdateMultiple() throws InvalidKeyException, SignatureException, IOException {
		// sign the data
		SignatureCodec signatureCodec = new H2HSignatureFactory().sign(protectionKey.getPrivate(), testData.buffer());

		// update (already belongs to the verification)
		int length = testData.buffer().readableBytes();
		ByteBuf slice1 = testData.buffer().copy(0, 3);
		ByteBuf slice2 = testData.buffer().copy(3, 4);
		ByteBuf slice3 = testData.buffer().copy(7, length - 7);

		H2HSignatureFactory signatureFactory = new H2HSignatureFactory();
		Signature signature = signatureFactory.update(protectionKey.getPublic(), slice1.nioBuffers());
		signature.update(slice2.array());
		signature.update(slice3.array());

		// verify the data with the signature
		Assert.assertTrue(signature.verify(signatureCodec.encode()));
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
