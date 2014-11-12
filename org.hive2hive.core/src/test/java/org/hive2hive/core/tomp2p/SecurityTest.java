package org.hive2hive.core.tomp2p;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.tomp2p.connection.ChannelClientConfiguration;
import net.tomp2p.connection.ChannelServerConfiguration;
import net.tomp2p.connection.DSASignatureFactory;
import net.tomp2p.connection.SignatureFactory;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.message.SignatureCodec;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.security.H2HSignatureCodec;
import org.hive2hive.core.security.H2HSignatureFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A test which test the content protection mechanisms (signing of put/remove messages) and data signing of
 * <code>TomP2P</code>. Tests should be completely independent of <code>Hive2Hive</code>.
 * 
 * @author Seppi
 */
public class SecurityTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = SecurityTest.class;
		beforeClass();
	}

	@Test
	public void testPutOverwriteWithoutContentProtectionKeys() throws IOException, ClassNotFoundException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();

		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		String testData = "data";
		Data data = new Data(testData).protectEntry();

		// put with protection key
		FuturePut futurePut1 = p1.put(lKey).data(cKey, data).domainKey(dKey).keyPair(keyPair).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// verify put from peer 1
		FutureGet futureGet1a = p1.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		Data retData = futureGet1a.data();
		assertEquals(testData, (String) retData.object());
		assertEquals(keyPair.getPublic(), retData.publicKey());

		// verify put from peer 2
		FutureGet futureGet1b = p2.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		retData = futureGet1b.data();
		assertEquals(testData, (String) retData.object());
		assertEquals(keyPair.getPublic(), retData.publicKey());

		// try a put without a protection key (through peer 2)
		Data data2 = new Data("data2");
		FuturePut futurePut2 = p2.put(lKey).data(cKey, data2).domainKey(dKey).start();
		futurePut2.awaitUninterruptibly();
		assertFalse(futurePut2.isSuccess());

		// verify that nothing changed from peer 1
		FutureGet futureGet2a = p1.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet2a.awaitUninterruptibly();
		assertTrue(futureGet2a.isSuccess());
		retData = futureGet2a.data();
		// should have been not modified
		assertEquals(testData, (String) retData.object());
		assertEquals(keyPair.getPublic(), retData.publicKey());

		// verify that nothing changed from peer 2
		FutureGet futureGet2b = p2.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet2b.awaitUninterruptibly();
		assertTrue(futureGet2b.isSuccess());
		retData = futureGet2b.data();
		// should have been not modified
		assertEquals(testData, (String) retData.object());
		assertEquals(keyPair.getPublic(), retData.publicKey());

		// overwrite
		String newTestData = "new data";
		data = new Data(newTestData).protectEntry();
		// sign put message with protection keys
		FuturePut futurePut4 = p1.put(lKey).data(cKey, data).keyPair(keyPair).domainKey(dKey).start();
		futurePut4.awaitUninterruptibly();
		Assert.assertTrue(futurePut4.isSuccess());

		// verify overwrite from peer 1
		FutureGet futureGet4a = p1.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet4a.awaitUninterruptibly();
		assertTrue(futureGet4a.isSuccess());
		retData = futureGet4a.data();
		assertEquals(keyPair.getPublic(), retData.publicKey());
		// should have been modified
		assertEquals(newTestData, (String) retData.object());

		// verify overwrite from peer 2
		FutureGet futureGet4b = p2.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet4b.awaitUninterruptibly();
		assertTrue(futureGet4b.isSuccess());
		retData = futureGet4b.data();
		assertEquals(keyPair.getPublic(), retData.publicKey());
		// should have been modified
		assertEquals(newTestData, (String) retData.object());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testPutSignedAndContentProtectedData() throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		SignatureFactory factory = new DSASignatureFactory();

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.createHash("based on");

		String testData = "data";
		Data data = new Data(testData).protectEntry().signatureFactory(factory).sign(keyPair);
		data.ttlSeconds(10000).addBasedOn(bKey);

		FuturePut futurePut1 = p1.put(lKey).data(cKey, data).domainKey(dKey).versionKey(vKey).keyPair(keyPair).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		Data retData = futureGet1a.data();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), factory));
		assertEquals(keyPair.getPublic(), retData.publicKey());

		FutureGet futureGet1b = p2.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		retData = futureGet1b.data();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), factory));
		assertEquals(keyPair.getPublic(), retData.publicKey());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testPutOverwriteWithWrongContentProtectionKeys() throws IOException, ClassNotFoundException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		SignatureFactory factory = new DSASignatureFactory();

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// initial put using content protection keys 1
		String testData1 = "data1";
		Data data = new Data(testData1).protectEntry();
		FuturePut futurePut1 = p1.put(lKey).data(cKey, data).domainKey(dKey).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		Data retData = futureGet1a.data();
		assertEquals(testData1, (String) retData.object());
		assertEquals(keyPair1.getPublic(), retData.publicKey());

		FutureGet futureGet1b = p2.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		retData = futureGet1b.data();
		assertEquals(testData1, (String) retData.object());
		assertEquals(keyPair1.getPublic(), retData.publicKey());

		// try to put with wrong content protection keys 2
		String testData2 = "data2";
		Data data2 = new Data(testData2).protectEntry().signatureFactory(factory).sign(keyPair2);
		FuturePut futurePut2 = p2.put(lKey).data(cKey, data2).domainKey(dKey).keyPair(keyPair2).start();
		futurePut2.awaitUninterruptibly();
		assertFalse(futurePut2.isSuccess());

		FutureGet futureGet2a = p1.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet2a.awaitUninterruptibly();
		assertTrue(futureGet2a.isSuccess());
		// should have been not modified
		retData = futureGet2a.data();
		assertEquals(testData1, (String) retData.object());
		assertEquals(keyPair1.getPublic(), retData.publicKey());

		FutureGet futureGet2b = p2.get(lKey).contentKey(cKey).domainKey(dKey).start();
		futureGet2b.awaitUninterruptibly();
		assertTrue(futureGet2b.isSuccess());
		// should have been not modified
		retData = futureGet2b.data();
		assertEquals(testData1, (String) retData.object());
		assertEquals(keyPair1.getPublic(), retData.publicKey());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testRemoveWithWrongOrWrongContentProtectionKeys() throws NoSuchAlgorithmException, IOException,
			InvalidKeyException, SignatureException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 cKey = Number160.createHash("content");

		// put with content protection keys 1
		String testData1 = "data1";
		Data data = new Data(testData1).protectEntry();
		FuturePut futurePut1 = p1.put(lKey).data(cKey, data).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// try to remove without content protection keys
		FutureRemove futureRemove1 = p1.remove(lKey).contentKey(cKey).start();
		futureRemove1.awaitUninterruptibly();
		assertFalse(futureRemove1.isSuccess());

		// verify failed remove
		FutureGet futureGet2 = p1.get(lKey).contentKey(cKey).start();
		futureGet2.awaitUninterruptibly();
		assertTrue(futureGet2.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2.data().object());
		assertEquals(keyPair1.getPublic(), futureGet2.data().publicKey());

		// try to remove with wrong content protection keys 2
		FutureRemove futureRemove2 = p1.remove(lKey).contentKey(cKey).keyPair(keyPair2).start();
		futureRemove2.awaitUninterruptibly();
		assertFalse(futureRemove2.isSuccess());

		// verify failed remove
		FutureGet futureGet3 = p1.get(lKey).contentKey(cKey).start();
		futureGet3.awaitUninterruptibly();
		assertTrue(futureGet3.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3.data().object());
		assertEquals(keyPair1.getPublic(), futureGet3.data().publicKey());

		// remove with correct content protection keys
		FutureRemove futureRemove4 = p1.remove(lKey).contentKey(cKey).keyPair(keyPair1).start();
		futureRemove4.awaitUninterruptibly();
		assertTrue(futureRemove4.isSuccess());

		// verify remove from peer 1
		FutureGet futureGet4a = p1.get(lKey).contentKey(cKey).start();
		futureGet4a.awaitUninterruptibly();
		assertFalse(futureGet4a.isSuccess());
		// should have been removed
		assertNull(futureGet4a.data());

		// verify remove from peer 2
		FutureGet futureGet4b = p2.get(lKey).contentKey(cKey).start();
		futureGet4b.awaitUninterruptibly();
		assertFalse(futureGet4b.isSuccess());
		// should have been removed
		assertNull(futureGet4b.data());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testRemoveVersionWithWrongContentProtectionKeys() throws NoSuchAlgorithmException, IOException,
			InvalidKeyException, SignatureException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");

		// put with content protection keys 1
		String testData1 = "data1";
		Data data = new Data(testData1).protectEntry();
		FuturePut futurePut1 = p1.put(lKey).data(cKey, data).versionKey(vKey).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// try to remove without content protection keys
		FutureRemove futureRemove1 = p1.remove(lKey).contentKey(cKey).versionKey(vKey).start();
		futureRemove1.awaitUninterruptibly();
		assertFalse(futureRemove1.isSuccess());

		// verify failed remove
		FutureGet futureGet2 = p1.get(lKey).contentKey(cKey).versionKey(vKey).start();
		futureGet2.awaitUninterruptibly();
		assertTrue(futureGet2.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2.data().object());
		assertEquals(keyPair1.getPublic(), futureGet2.data().publicKey());

		// try to remove with wrong content protection keys 2
		FutureRemove futureRemove2 = p1.remove(lKey).contentKey(cKey).versionKey(vKey).keyPair(keyPair2).start();
		futureRemove2.awaitUninterruptibly();
		assertFalse(futureRemove2.isSuccess());

		// verify failed remove
		FutureGet futureGet3 = p1.get(lKey).contentKey(cKey).versionKey(vKey).start();
		futureGet3.awaitUninterruptibly();
		assertTrue(futureGet3.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3.data().object());
		assertEquals(keyPair1.getPublic(), futureGet3.data().publicKey());

		// remove with correct content protection keys
		FutureRemove futureRemove4 = p1.remove(lKey).contentKey(cKey).versionKey(vKey).keyPair(keyPair1).start();
		futureRemove4.awaitUninterruptibly();
		assertTrue(futureRemove4.isSuccess());

		// verify remove from peer 1
		FutureGet futureGet4a = p1.get(lKey).contentKey(cKey).versionKey(vKey).start();
		futureGet4a.awaitUninterruptibly();
		assertFalse(futureGet4a.isSuccess());
		// should have been removed
		assertNull(futureGet4a.data());

		// verify remove from peer 2
		FutureGet futureGet4b = p2.get(lKey).contentKey(cKey).versionKey(vKey).start();
		futureGet4b.awaitUninterruptibly();
		assertFalse(futureGet4b.isSuccess());
		// should have been removed
		assertNull(futureGet4b.data());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testRemoveWithFromToContentProtectedEntry() throws NoSuchAlgorithmException, IOException,
			InvalidKeyException, SignatureException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair key1 = gen.generateKeyPair();
		KeyPair key2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");

		// put with content protection keys 1
		String testData1 = "data1";
		Data data = new Data(testData1).protectEntry();
		FuturePut futurePut1 = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(key1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// try to remove without content protection keys using from/to
		FutureRemove futureRemove1 = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
		futureRemove1.awaitUninterruptibly();
		assertFalse(futureRemove1.isSuccess());

		// verify failed remove
		FutureGet futureGet2 = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start();
		futureGet2.awaitUninterruptibly();
		assertTrue(futureGet2.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2.data().object());
		assertEquals(key1.getPublic(), futureGet2.data().publicKey());

		// remove with wrong content protection keys 2 using from/to
		FutureRemove futureRemove2a = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key2).start();
		futureRemove2a.awaitUninterruptibly();
		assertFalse(futureRemove2a.isSuccess());

		// verify failed remove
		FutureGet futureGet3 = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start();
		futureGet3.awaitUninterruptibly();
		assertTrue(futureGet3.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3.data().object());
		assertEquals(key1.getPublic(), futureGet3.data().publicKey());

		// remove with correct content protection keys 1 using from/to
		FutureRemove futureRemove4 = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key1).start();
		futureRemove4.awaitUninterruptibly();
		assertTrue(futureRemove4.isSuccess());

		// verify remove
		FutureGet futureGet4 = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start();
		futureGet4.awaitUninterruptibly();
		// we did not find the data
		Assert.assertTrue(futureGet4.isFailed());
		// should have been removed
		assertNull(futureGet4.data());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testRemoveWithFromToSeveralVersionWithContentProtection() throws NoSuchAlgorithmException, IOException,
			InvalidKeyException, SignatureException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair key = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// put version 1 with content protection keys
		Data data1 = new Data("data1").protectEntry();
		Number160 vKey1 = Number160.createHash("version1");
		FuturePut futurePut1 = p1.put(lKey).domainKey(dKey).data(cKey, data1).versionKey(vKey1).keyPair(key).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// put version 2 basing on version 1 with content protection keys
		Data data2 = new Data("data2").addBasedOn(vKey1).protectEntry();
		Number160 vKey2 = Number160.createHash("version2");
		FuturePut futurePut2 = p1.put(lKey).domainKey(dKey).data(cKey, data2).versionKey(vKey2).keyPair(key).start();
		futurePut2.awaitUninterruptibly();
		assertTrue(futurePut2.isSuccess());

		// remove with correct content protection keys using from/to
		FutureRemove futureRemove = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key).start();
		futureRemove.awaitUninterruptibly();
		assertTrue(futureRemove.isSuccess());

		// verify remove of version 1
		FutureGet futureGet4a = p2.get(lKey).contentKey(cKey).versionKey(vKey1).start();
		futureGet4a.awaitUninterruptibly();
		// we did not find the data
		Assert.assertTrue(futureGet4a.isFailed());
		// should have been removed
		assertNull(futureGet4a.data());

		// verify remove of version 2
		FutureGet futureGet4b = p2.get(lKey).contentKey(cKey).versionKey(vKey2).start();
		futureGet4b.awaitUninterruptibly();
		// we did not find the data
		Assert.assertTrue(futureGet4b.isFailed());
		// should have been removed
		assertNull(futureGet4b.data());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeProtectionKeyWithoutDataSignature() throws IOException, ClassNotFoundException,
			NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash(2); // same like node 2
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// initial put with protection keys 1
		Data data1 = new Data("data1").protectEntry();
		FuturePut futurePut1 = p1.put(lKey).data(cKey, data1).domainKey(dKey).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// change protection keys to protection keys 2, create meta data
		Data data3 = new Data().protectEntry().publicKey(keyPair2.getPublic()).duplicateMeta();
		// use the old protection key 1 to sign the message
		FuturePut futurePut3 = p1.put(lKey).domainKey(dKey).putMeta().data(cKey, data3).keyPair(keyPair1).start();
		futurePut3.awaitUninterruptibly();
		assertTrue(futurePut3.isSuccess());

		// verify that nothing changed
		FutureGet futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).start().awaitUninterruptibly();
		assertEquals("data1", (String) futureGet.data().object());
		// verify new content protection keys 2
		assertEquals(keyPair2.getPublic(), futureGet.data().publicKey());

		// overwrite with protection keys 2
		Data data4 = new Data("data4").protectEntry();
		FuturePut futurePut4 = p1.put(lKey).data(cKey, data4).domainKey(dKey).keyPair(keyPair2).start();
		futurePut4.awaitUninterruptibly();
		assertTrue(futurePut4.isSuccess());

		// verify overwrite
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).start().awaitUninterruptibly();
		assertEquals("data4", (String) futureGet.data().object());
		assertEquals(keyPair2.getPublic(), futureGet.data().publicKey());

		// try to overwrite without protection keys (expected to fail)
		Data data5A = new Data("data5A");
		FuturePut futurePut5A = p1.put(lKey).data(cKey, data5A).domainKey(dKey).start();
		futurePut5A.awaitUninterruptibly();
		assertFalse(futurePut5A.isSuccess());

		// verify that nothing changed
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).start().awaitUninterruptibly();
		assertEquals("data4", (String) futureGet.data().object());
		assertEquals(keyPair2.getPublic(), futureGet.data().publicKey());

		// try to overwrite with wrong protection keys 1 (expected to fail)
		Data data5B = new Data("data5B").protectEntry();
		FuturePut futurePut5B = p1.put(lKey).data(cKey, data5B).domainKey(dKey).keyPair(keyPair1).start();
		futurePut5B.awaitUninterruptibly();
		assertFalse(futurePut5B.isSuccess());

		// verify that nothing changed
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).start().awaitUninterruptibly();
		assertEquals("data4", (String) futureGet.data().object());
		assertEquals(keyPair2.getPublic(), futureGet.data().publicKey());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeProtectionKeyWithVersionKeyWithoutDataSignature() throws NoSuchAlgorithmException, IOException,
			ClassNotFoundException, InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.createHash("based on");

		// initial put with protection keys 1
		Data data1 = new Data("data1").addBasedOn(bKey).protectEntry();
		FuturePut futurePut1 = p1.put(lKey).data(cKey, data1).domainKey(dKey).versionKey(vKey).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// overwrite with protection keys 1
		Data data2 = new Data("data2").addBasedOn(bKey).protectEntry();
		FuturePut futurePut2 = p1.put(lKey).data(cKey, data2).domainKey(dKey).versionKey(vKey).keyPair(keyPair1).start();
		futurePut2.awaitUninterruptibly();
		assertTrue(futurePut2.isSuccess());

		// verify overwrite
		FutureGet futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start().awaitUninterruptibly();
		assertEquals("data2", (String) futureGet.data().object());
		assertEquals(keyPair1.getPublic(), futureGet.data().publicKey());

		// try to overwrite without protection keys (expected to fail)
		Data data2A = new Data("data2A").addBasedOn(bKey);
		FuturePut futurePut2A = p1.put(lKey).data(cKey, data2A).domainKey(dKey).versionKey(vKey).start();
		futurePut2A.awaitUninterruptibly();
		assertFalse(futurePut2A.isSuccess());

		// verify that nothing changed
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start().awaitUninterruptibly();
		assertEquals("data2", (String) futureGet.data().object());
		assertEquals(keyPair1.getPublic(), futureGet.data().publicKey());

		// try to overwrite with wrong protection keys 2 (expected to fail)
		Data data2B = new Data("data2B").addBasedOn(bKey).protectEntry();
		FuturePut futurePut2B = p1.put(lKey).data(cKey, data2B).domainKey(dKey).versionKey(vKey).keyPair(keyPair2).start();
		futurePut2B.awaitUninterruptibly();
		assertFalse(futurePut2B.isSuccess());

		// verify that nothing changed
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start().awaitUninterruptibly();
		assertEquals("data2", (String) futureGet.data().object());
		assertEquals(keyPair1.getPublic(), futureGet.data().publicKey());

		// change protection keys to protection keys 2, create meta data
		Data data3 = new Data().protectEntry().publicKey(keyPair2.getPublic()).duplicateMeta();
		// use the old protection key 1 to sign the message
		FuturePut futurePut3 = p1.put(lKey).domainKey(dKey).versionKey(vKey).putMeta().data(cKey, data3).keyPair(keyPair1)
				.start();
		futurePut3.awaitUninterruptibly();
		assertTrue(futurePut3.isSuccess());

		// verify change
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start().awaitUninterruptibly();
		// data stays the same
		assertEquals("data2", (String) futureGet.data().object());
		// should be new protection key
		assertEquals(keyPair2.getPublic(), futureGet.data().publicKey());

		// overwrite with protection keys 2
		Data data4 = new Data("data4").addBasedOn(bKey).protectEntry();
		FuturePut futurePut4 = p1.put(lKey).data(cKey, data4).domainKey(dKey).versionKey(vKey).keyPair(keyPair2).start();
		futurePut4.awaitUninterruptibly();
		assertTrue(futurePut4.isSuccess());

		// verify overwrite
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start().awaitUninterruptibly();
		assertEquals("data4", (String) futureGet.data().object());
		assertEquals(keyPair2.getPublic(), futureGet.data().publicKey());

		// try to overwrite without protection keys (expected to fail)
		Data data5A = new Data("data5A").addBasedOn(bKey);
		FuturePut futurePut5A = p1.put(lKey).data(cKey, data5A).domainKey(dKey).versionKey(vKey).start();
		futurePut5A.awaitUninterruptibly();
		assertFalse(futurePut5A.isSuccess());

		// verify that nothing changed
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start().awaitUninterruptibly();
		assertEquals("data4", (String) futureGet.data().object());
		assertEquals(keyPair2.getPublic(), futureGet.data().publicKey());

		// try to overwrite with wrong protection keys 1 (expected to fail)
		Data data5B = new Data("data5B").addBasedOn(bKey).protectEntry();
		FuturePut futurePut5B = p1.put(lKey).data(cKey, data5B).domainKey(dKey).versionKey(vKey).keyPair(keyPair1).start();
		futurePut5B.awaitUninterruptibly();
		assertFalse(futurePut5B.isSuccess());

		// verify that nothing changed
		futureGet = p2.get(lKey).contentKey(cKey).domainKey(dKey).versionKey(vKey).start().awaitUninterruptibly();
		assertEquals("data4", (String) futureGet.data().object());
		assertEquals(keyPair2.getPublic(), futureGet.data().publicKey());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeDataSignatureWithReusedHashWithoutContentProtection() throws NoSuchAlgorithmException,
			IOException, ClassNotFoundException, InvalidKeyException, SignatureException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

		// create custom RSA factories
		SignatureFactory factory = new H2HSignatureFactory();
		SignatureCodec codec = new H2HSignatureCodec();

		// replace default signature factories
		ChannelClientConfiguration clientConfig = PeerBuilder.createDefaultChannelClientConfiguration();
		clientConfig.signatureFactory(factory);
		ChannelServerConfiguration serverConfig = PeerBuilder.createDefaultChannelServerConfiguration();
		serverConfig.signatureFactory(factory);

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).start()).start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.createHash("based on");
		int ttl = 10;

		// initial put with keys 1 and data signature
		Data data = new Data("data").signatureFactory(factory).sign(keyPair1);
		data.ttlSeconds(ttl).addBasedOn(bKey);
		FuturePut futurePut = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).start();
		futurePut.awaitUninterruptibly();
		Assert.assertTrue(futurePut.isSuccess());

		// create signature with keys 1 having the data object
		byte[] signature1 = factory.sign(keyPair1.getPrivate(), data.buffer()).encode();

		// decrypt signature to get hash of the object
		Cipher rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.DECRYPT_MODE, keyPair1.getPublic());
		byte[] hash = rsa.doFinal(signature1);

		// encrypt hash with new key pair to get the new signature (without having the data object)
		rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.ENCRYPT_MODE, keyPair2.getPrivate());
		byte[] signatureNew = rsa.doFinal(hash);

		// verify data signature
		Assert.assertTrue(p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly()
				.data().verify(keyPair1.getPublic(), factory));

		// change data signature to keys 2, assign the reused hash from signature
		data = new Data().ttlSeconds(ttl).signature(codec.decode(signatureNew));
		// don't forget to set signed flag, create meta data
		data.signed(true).duplicateMeta();
		FuturePut futurePutMeta = p1.put(lKey).domainKey(dKey).putMeta().data(cKey, data).versionKey(vKey).start();
		futurePutMeta.awaitUninterruptibly();
		Assert.assertTrue(futurePutMeta.isSuccess());

		// verify new data signature
		Data retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertTrue(retData.verify(keyPair2.getPublic(), factory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeDataSignatureWithReusedHashWithContentProtection() throws NoSuchAlgorithmException, IOException,
			ClassNotFoundException, InvalidKeyException, SignatureException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

		// create custom RSA factories
		SignatureFactory factory = new H2HSignatureFactory();
		SignatureCodec codec = new H2HSignatureCodec();

		// replace default signature factories
		ChannelClientConfiguration clientConfig = PeerBuilder.createDefaultChannelClientConfiguration();
		clientConfig.signatureFactory(factory);
		ChannelServerConfiguration serverConfig = PeerBuilder.createDefaultChannelServerConfiguration();
		serverConfig.signatureFactory(factory);

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).start()).start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.createHash("based on");
		int ttl = 10;

		// initial put with data signature and entry protection
		Data intialData = new Data("data").protectEntry();
		intialData.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using content protection key 1 to sign message
		FuturePut futureIntialPut = p1.put(lKey).domainKey(dKey).data(cKey, intialData).versionKey(vKey).keyPair(keyPair1)
				.start();
		futureIntialPut.awaitUninterruptibly();
		Assert.assertTrue(futureIntialPut.isSuccess());

		// verify put
		Data retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify data signature
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		// try to overwrite without content protection and data signature (expected to fail)
		Data data = new Data("dataA");
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using content protection key 1 to sign message
		FuturePut futureTryOverwrite = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).start();
		futureTryOverwrite.awaitUninterruptibly();
		Assert.assertFalse(futureTryOverwrite.isSuccess());

		// verify that nothing changed
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify that data signature is still the same
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		// try to overwrite with wrong protection keys 2 and data signature (expected to fail)
		data = new Data("dataB").protectEntry();
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using wrong content protection keys 2 to sign message
		futureTryOverwrite = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(keyPair2).start();
		futureTryOverwrite.awaitUninterruptibly();
		Assert.assertFalse(futureTryOverwrite.isSuccess());

		// verify that nothing changed
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify that data signature is still the same
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		// try to overwrite without content protection and without data signature (expected to fail)
		data = new Data("dataC");
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using wrong content protection keys 2 to sign message
		futureTryOverwrite = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).start();
		futureTryOverwrite.awaitUninterruptibly();
		Assert.assertFalse(futureTryOverwrite.isSuccess());

		// verify that nothing changed
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify that data signature is still the same
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		// try to overwrite with wrong protection keys 2 and without data signature (expected to fail)
		data = new Data("dataD").protectEntry();
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using wrong content protection keys 2 to sign message
		futureTryOverwrite = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(keyPair2).start();
		futureTryOverwrite.awaitUninterruptibly();
		Assert.assertFalse(futureTryOverwrite.isSuccess());

		// verify that nothing changed
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify that data signature is still the same
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		// overwrite with content protection keys 1 and no data signature
		intialData = new Data("data2").protectEntry();
		intialData.ttlSeconds(ttl).addBasedOn(bKey);
		// put using content protection key 1 to sign message
		FuturePut futureOverwrite1 = p1.put(lKey).domainKey(dKey).data(cKey, intialData).versionKey(vKey).keyPair(keyPair1)
				.start();
		futureOverwrite1.awaitUninterruptibly();
		Assert.assertTrue(futureOverwrite1.isSuccess());

		// verify overwrite
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data2", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify no signature
		Assert.assertNull(retData.signature());

		// overwrite with content protection key1 and with data signature
		intialData = new Data("data3").protectEntry();
		intialData.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using content protection key 1 to sign message
		FuturePut futureOverwrite2 = p1.put(lKey).domainKey(dKey).data(cKey, intialData).versionKey(vKey).keyPair(keyPair1)
				.start();
		futureOverwrite2.awaitUninterruptibly();
		Assert.assertTrue(futureOverwrite2.isSuccess());

		// verify overwrite
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data3", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify that data signature is still the same
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		// create signature with keys 1 having the data object
		byte[] signature1 = factory.sign(keyPair1.getPrivate(), intialData.buffer()).encode();

		// decrypt signature to get hash of the object
		Cipher rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.DECRYPT_MODE, keyPair1.getPublic());
		byte[] hash = rsa.doFinal(signature1);

		// encrypt hash with new key pair to get the new signature (without having the data object)
		rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.ENCRYPT_MODE, keyPair2.getPrivate());
		byte[] signatureNew = rsa.doFinal(hash);

		// change data signature to keys 2, assign the reused hash from signature
		data = new Data().ttlSeconds(ttl).signature(codec.decode(signatureNew)).protectEntry();
		// don't forget to set signed flag, create meta data
		data.signed(true).duplicateMeta();
		// put meta using content content protection key 1 to sign message
		FuturePut futurePutMeta = p1.put(lKey).domainKey(dKey).putMeta().data(cKey, data).versionKey(vKey).keyPair(keyPair1)
				.start();
		futurePutMeta.awaitUninterruptibly();
		Assert.assertTrue(futurePutMeta.isSuccess());

		// verify change
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data3", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify new data signature
		Assert.assertTrue(retData.verify(keyPair2.getPublic(), factory));

		// overwrite with content protection key 1 and data signature
		data = new Data("data4").protectEntry();
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using content content protection key 1 to sign message
		FuturePut futureOverwrite3 = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(keyPair1)
				.start();
		futureOverwrite3.awaitUninterruptibly();
		Assert.assertTrue(futureOverwrite3.isSuccess());

		// verify change
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data4", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify that data signature wasn't changed
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeDataSignatureAndChangeContentProtectionSimultanously() throws NoSuchAlgorithmException,
			IOException, ClassNotFoundException, InvalidKeyException, SignatureException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

		// create custom RSA factories
		SignatureFactory factory = new H2HSignatureFactory();

		// replace default signature factories
		ChannelClientConfiguration clientConfig = PeerBuilder.createDefaultChannelClientConfiguration();
		clientConfig.signatureFactory(factory);
		ChannelServerConfiguration serverConfig = PeerBuilder.createDefaultChannelServerConfiguration();
		serverConfig.signatureFactory(factory);

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).start()).start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.createHash("based on");
		int ttl = 10;

		// initial put with data signature and entry protection
		Data data = new Data("data1").protectEntry();
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using content protection key 1 to sign message
		FuturePut futureIntialPut = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(keyPair1).start();
		futureIntialPut.awaitUninterruptibly();
		Assert.assertTrue(futureIntialPut.isSuccess());

		// verify put
		Data retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify data signature
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		// change data signature to keys 2 using same data, sign with new key 2
		data = new Data("data1").ttlSeconds(ttl).protectEntry().signatureFactory(factory).sign(keyPair2);
		// change content protection keys to keys 2
		// data.publicKey(keyPair2.getPublic()); is already done with data.sign(...)
		// create meta data
		data.duplicateMeta();
		// put meta using content content protection key 1 to sign message
		FuturePut futurePutMeta = p1.put(lKey).domainKey(dKey).putMeta().data(cKey, data).versionKey(vKey).keyPair(keyPair1)
				.start();
		futurePutMeta.awaitUninterruptibly();
		Assert.assertTrue(futurePutMeta.isSuccess());

		// verify change
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		// verify new content protection keys 2
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey());
		// verify new data signature
		Assert.assertTrue(retData.verify(keyPair2.getPublic(), factory));

		// try overwrite with content protection key 1 and data signature (expected to fail)
		data = new Data("data2").protectEntry();
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair2);
		// put using content wrong protection keys 1 to sign message
		FuturePut futureOverwrite3 = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(keyPair1)
				.start();
		futureOverwrite3.awaitUninterruptibly();
		Assert.assertFalse(futureOverwrite3.isSuccess());

		// verify that nothing changed
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey());
		// verify not changed signature
		Assert.assertTrue(retData.verify(keyPair2.getPublic(), factory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeDataSignatureWithReusedHashAndChangeContentProtectionSimultanously()
			throws NoSuchAlgorithmException, IOException, ClassNotFoundException, InvalidKeyException, SignatureException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

		// create custom RSA factories
		SignatureFactory factory = new H2HSignatureFactory();
		SignatureCodec codec = new H2HSignatureCodec();

		// replace default signature factories
		ChannelClientConfiguration clientConfig = PeerBuilder.createDefaultChannelClientConfiguration();
		clientConfig.signatureFactory(factory);
		ChannelServerConfiguration serverConfig = PeerBuilder.createDefaultChannelServerConfiguration();
		serverConfig.signatureFactory(factory);

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).start()).start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.channelClientConfiguration(clientConfig).channelServerConfiguration(serverConfig).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.createHash("based on");
		int ttl = 10;

		// initial put with data signature and entry protection
		Data data = new Data("data1").protectEntry();
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair1);
		// put using content protection key 1 to sign message
		FuturePut futureIntialPut = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(keyPair1).start();
		futureIntialPut.awaitUninterruptibly();
		Assert.assertTrue(futureIntialPut.isSuccess());

		// verify put
		Data retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		// verify content protection keys 1 is set
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());
		// verify data signature
		Assert.assertTrue(retData.verify(keyPair1.getPublic(), factory));

		// create signature with keys 1 having the data object
		byte[] signature1 = factory.sign(keyPair1.getPrivate(), data.buffer()).encode();

		// decrypt signature to get hash of the object
		Cipher rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.DECRYPT_MODE, keyPair1.getPublic());
		byte[] hash = rsa.doFinal(signature1);

		// encrypt hash with new key pair to get the new signature (without having the data object)
		rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.ENCRYPT_MODE, keyPair2.getPrivate());
		byte[] signatureNew = rsa.doFinal(hash);

		// change data signature to keys 2, assign the reused hash from signature
		data = new Data().ttlSeconds(ttl).signature(codec.decode(signatureNew)).protectEntry();
		// don't forget to set signed flag
		data.signed(true);
		// change the content protection keys to 2
		data.publicKey(keyPair2.getPublic());
		// create meta data
		data.duplicateMeta();
		// put meta using content content protection key 1 to sign message
		FuturePut futurePutMeta = p1.put(lKey).domainKey(dKey).putMeta().data(cKey, data).versionKey(vKey).keyPair(keyPair1)
				.start();
		futurePutMeta.awaitUninterruptibly();
		Assert.assertTrue(futurePutMeta.isSuccess());

		// verify change
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		// verify change to content protection keys 2
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey());
		// verify new data signature
		Assert.assertTrue(retData.verify(keyPair2.getPublic(), factory));

		// try overwrite with content protection key 1 and data signature (exptected to fail)
		data = new Data("data2").protectEntry();
		data.ttlSeconds(ttl).addBasedOn(bKey).signatureFactory(factory).sign(keyPair2);
		// put using content wrong protection keys 1 to sign message
		FuturePut futureOverwrite3 = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(keyPair1)
				.start();
		futureOverwrite3.awaitUninterruptibly();
		Assert.assertFalse(futureOverwrite3.isSuccess());

		// verify that nothing changed
		retData = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey());
		// verify not changed signature
		Assert.assertTrue(retData.verify(keyPair2.getPublic(), factory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testContentProtectionAppliesToAllVersionKeys() throws NoSuchAlgorithmException, IOException,
			ClassNotFoundException, InvalidKeyException, SignatureException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// put version 1 with protection keys 1
		Data data1 = new Data("data1").protectEntry();
		Number160 vKey1 = Number160.ZERO;
		FuturePut futurePut1 = p1.put(lKey).data(cKey, data1).domainKey(dKey).versionKey(vKey1).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// verify put of version 1
		Data retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey1).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		// verify content protection keys 1 is set
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());

		// try to overwrite version 1 without protection keys (expected to fail)
		Data data1A = new Data("data1A");
		FuturePut futurePut1A = p1.put(lKey).data(cKey, data1A).domainKey(dKey).versionKey(vKey1).start();
		futurePut1A.awaitUninterruptibly();
		assertFalse(futurePut1A.isSuccess());

		// verify that nothing changed
		retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey1).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());

		// try to overwrite version 1 with wrong protection keys 2 (expected to fail)
		Data data1B = new Data("data1B").protectEntry();
		FuturePut futurePut1B = p1.put(lKey).data(cKey, data1B).domainKey(dKey).versionKey(vKey1).keyPair(keyPair2).start();
		futurePut1B.awaitUninterruptibly();
		assertFalse(futurePut1B.isSuccess());

		// verify that nothing changed
		retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey1).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());

		// overwrite version 1 with protection keys 1
		Data data1Overwrite = new Data("data1Overwrite").protectEntry();
		FuturePut futurePutOverwrite = p1.put(lKey).data(cKey, data1Overwrite).domainKey(dKey).versionKey(vKey1)
				.keyPair(keyPair1).start();
		futurePutOverwrite.awaitUninterruptibly();
		assertTrue(futurePutOverwrite.isSuccess());

		// verify overwrite
		retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey1).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1Overwrite", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());

		// try to put new version 2 (basing on version 1) with wrong protection keys 2 (expected to fail)
		Data data2 = new Data("data2").addBasedOn(vKey1).protectEntry();
		// version 2 takes new version key
		Number160 vKey2 = Number160.createHash("version2");
		FuturePut futurePut2 = p1.put(lKey).data(cKey, data2).domainKey(dKey).versionKey(vKey2).keyPair(keyPair2).start();
		futurePut2.awaitUninterruptibly();
		assertFalse(futurePut2.isSuccess());

		// verify no put of version 2
		assertNull(p2.get(lKey).contentKey(cKey).versionKey(vKey2).domainKey(dKey).start().awaitUninterruptibly().data());

		// put new version 3 (basing on version 1) with correct protection keys 1
		Data data3 = new Data("data3").addBasedOn(vKey1).protectEntry();
		Number160 vKey3 = Number160.createHash("version3");
		FuturePut futurePut3 = p1.put(lKey).data(cKey, data3).domainKey(dKey).versionKey(vKey3).keyPair(keyPair1).start();
		futurePut3.awaitUninterruptibly();
		assertTrue(futurePut3.isSuccess());

		// verify put of version 3
		assertEquals("data3", (String) p2.get(lKey).contentKey(cKey).versionKey(vKey3).domainKey(dKey).start()
				.awaitUninterruptibly().data().object());

		retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey3).start().awaitUninterruptibly().data();
		Assert.assertEquals("data3", (String) retData.object());
		Assert.assertEquals(keyPair1.getPublic(), retData.publicKey());

		// try to put a version X in version key range of version 1 and 3 with wrong protection keys 2
		// (expected to fail)
		Data dataX = new Data("dataX").protectEntry();
		Number160 vKeyX = Number160.createHash("versionX");
		FuturePut futurePut4 = p1.put(lKey).data(cKey, dataX).domainKey(dKey).versionKey(vKeyX).keyPair(keyPair2).start();
		futurePut4.awaitUninterruptibly();
		assertFalse(futurePut4.isSuccess());

		// verify no put of version X
		assertNull(p2.get(lKey).contentKey(cKey).versionKey(vKeyX).domainKey(dKey).start().awaitUninterruptibly().data());

		// try to put random version Y in version key range of version 1 and 3 without protection keys
		// (expected to fail)
		Data dataY = new Data("dataY").protectEntry();
		Number160 vKeyY = Number160.createHash("versionX");
		futurePut4 = p1.put(lKey).data(cKey, dataY).domainKey(dKey).versionKey(vKeyY).start();
		futurePut4.awaitUninterruptibly();
		assertFalse(futurePut4.isSuccess());

		// verify no put of version Y
		assertNull(p2.get(lKey).contentKey(cKey).versionKey(vKeyY).domainKey(dKey).start().awaitUninterruptibly().data());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	@Ignore
	public void testContentProtectionChangeAppliesToAllVersionKeys() throws NoSuchAlgorithmException, IOException,
			ClassNotFoundException, InvalidKeyException, SignatureException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// put version 1 with protection keys 1
		Data data1 = new Data("data1").protectEntry();
		Number160 vKey1 = Number160.ZERO;
		FuturePut futurePut1 = p1.put(lKey).data(cKey, data1).domainKey(dKey).versionKey(vKey1).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// put new version 2 (basing on version 1) with protection keys 1
		Data data2 = new Data("data2").addBasedOn(vKey1).protectEntry();
		// version 2 takes new version key
		Number160 vKey2 = Number160.createHash("version2");
		FuturePut futurePut2 = p1.put(lKey).data(cKey, data2).domainKey(dKey).versionKey(vKey2).keyPair(keyPair1).start();
		futurePut2.awaitUninterruptibly();
		assertTrue(futurePut2.isSuccess());

		// change protection key on version 1 with a put meta
		Data dataMeta = new Data().protectEntry().publicKey(keyPair2.getPublic()).duplicateMeta();
		// use the old protection key 1 to sign the message
		FuturePut futurePutMeta = p1.put(lKey).domainKey(dKey).versionKey(vKey1).putMeta().data(cKey, dataMeta)
				.keyPair(keyPair1).start();
		futurePutMeta.awaitUninterruptibly();
		assertTrue(futurePutMeta.isSuccess());

		// verify change at version 1
		Data retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey1).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1", (String) retData.object());
		// verify change of content protection keys to 2
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey());

		// verify change at version 2
		retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey2).start().awaitUninterruptibly().data();
		Assert.assertEquals("data2", (String) retData.object());
		// verify change of content protection keys to 2
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey()); // <==== is keypair1 but should be
																		// keypair2

		// overwrite version 1 with protection keys 2
		Data data1Overwrite = new Data("data1Overwrite").protectEntry();
		FuturePut futurePut1Overwrite = p1.put(lKey).data(cKey, data1Overwrite).domainKey(dKey).versionKey(vKey1)
				.keyPair(keyPair2).start();
		futurePut1Overwrite.awaitUninterruptibly();
		assertTrue(futurePut1Overwrite.isSuccess());

		// verify overwrite version 1
		retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey1).start().awaitUninterruptibly().data();
		Assert.assertEquals("data1Overwrite", (String) retData.object());
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey());

		// overwrite version 2 with protection keys 2
		Data data2Overwrite = new Data("data2Overwrite").protectEntry();
		FuturePut futurePut2Overwrite = p1.put(lKey).data(cKey, data2Overwrite).domainKey(dKey).versionKey(vKey2)
				.keyPair(keyPair2).start();
		futurePut2Overwrite.awaitUninterruptibly();
		assertTrue(futurePut2Overwrite.isSuccess());

		// verify overwrite version 2
		retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey2).start().awaitUninterruptibly().data();
		Assert.assertEquals("data2Overwrite", (String) retData.object());
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey());

		// put new version 3 (basing on version 2) with protection keys 2
		Data data3 = new Data("data3").addBasedOn(vKey2).protectEntry();
		Number160 vKey3 = Number160.createHash("version3");
		FuturePut futurePut3 = p1.put(lKey).data(cKey, data3).domainKey(dKey).versionKey(vKey3).keyPair(keyPair2).start();
		futurePut3.awaitUninterruptibly();
		assertTrue(futurePut3.isSuccess());

		// verify put version 3
		retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey3).start().awaitUninterruptibly().data();
		Assert.assertEquals("data3", (String) retData.object());
		Assert.assertEquals(keyPair2.getPublic(), retData.publicKey());

		// try to put a version X in version key range of version 1, 2 and 3 with wrong protection keys 1
		// (expected to fail)
		Data dataX = new Data("dataX").protectEntry();
		Number160 vKeyX = Number160.createHash("versionX");
		FuturePut futurePut4 = p1.put(lKey).data(cKey, dataX).domainKey(dKey).versionKey(vKeyX).keyPair(keyPair1).start();
		futurePut4.awaitUninterruptibly();
		assertFalse(futurePut4.isSuccess());

		// verify no put of version X
		assertNull(p2.get(lKey).contentKey(cKey).versionKey(vKeyX).domainKey(dKey).start().awaitUninterruptibly().data());

		// try to put random version Y in version key range of version 1, 2 and 3 without protection keys
		// (expected to fail)
		Data dataY = new Data("dataY").protectEntry();
		Number160 vKeyY = Number160.createHash("versionX");
		futurePut4 = p1.put(lKey).data(cKey, dataY).domainKey(dKey).versionKey(vKeyY).start();
		futurePut4.awaitUninterruptibly();
		assertFalse(futurePut4.isSuccess());

		// verify no put of version Y
		assertNull(p2.get(lKey).contentKey(cKey).versionKey(vKeyY).domainKey(dKey).start().awaitUninterruptibly().data());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
