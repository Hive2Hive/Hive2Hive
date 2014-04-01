package org.hive2hive.core.test.tomp2p;

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
import net.tomp2p.connection.ChannelServerConficuration;
import net.tomp2p.connection.DSASignatureFactory;
import net.tomp2p.connection.SignatureFactory;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.message.SignatureCodec;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.p2p.RSASignatureCodec;
import net.tomp2p.p2p.RSASignatureFactory;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test which test the content protection mechanisms of <code>TomP2P</code>. Tests should be completely
 * independent of <code>Hive2Hive</code>.
 * 
 * @author Seppi
 */
public class ContentProtectionTest extends H2HJUnitTest {

	private final SignatureFactory signatureFactory = new DSASignatureFactory();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ContentProtectionTest.class;
		beforeClass();
	}

	/**
	 * Tests if a protected entry can be overwritten without the according key.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 */
	@Test
	public void testPut1() throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4838).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		String testData = "data";
		Data data = new Data(testData).setProtectedEntry().sign(keyPair, signatureFactory);

		// initial put with protection key
		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).setDomainKey(dKey).keyPair(keyPair).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		Data retData = futureGet1a.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		FutureGet futureGet1b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		retData = futureGet1b.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		// try a put without a protection key (through peer 2)
		Data data2 = new Data("data2");
		FuturePut futurePut2 = p2.put(lKey).setData(cKey, data2).setDomainKey(dKey).start();
		futurePut2.awaitUninterruptibly();
		assertFalse(futurePut2.isSuccess());

		FutureGet futureGet2a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet2a.awaitUninterruptibly();
		assertTrue(futureGet2a.isSuccess());
		retData = futureGet2a.getData();
		// should have been not modified
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		FutureGet futureGet2b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet2b.awaitUninterruptibly();
		assertTrue(futureGet2b.isSuccess());
		retData = futureGet2b.getData();
		// should have been not modified
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		// try a put without a protection key (through peer 1)
		Data data3 = new Data("data3");
		FuturePut futurePut3 = p2.put(lKey).setData(cKey, data3).setDomainKey(dKey).start();
		futurePut3.awaitUninterruptibly();
		assertFalse(futurePut3.isSuccess());

		FutureGet futureGet3a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet3a.awaitUninterruptibly();
		assertTrue(futureGet3a.isSuccess());
		retData = futureGet3a.getData();
		// should have been not modified
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		FutureGet futureGet3b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet3b.awaitUninterruptibly();
		assertTrue(futureGet3b.isSuccess());
		retData = futureGet3b.getData();
		// should have been not modified
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		// now we put a signed data object
		String newTestData = "new data";
		data = new Data(newTestData).setProtectedEntry().sign(keyPair, signatureFactory);
		FuturePut futurePut4 = p1.put(lKey).setData(cKey, data).keyPair(keyPair).setDomainKey(dKey).start();
		futurePut4.awaitUninterruptibly();
		Assert.assertTrue(futurePut4.isSuccess());

		FutureGet futureGet4a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet4a.awaitUninterruptibly();
		assertTrue(futureGet4a.isSuccess());
		retData = futureGet4a.getData();
		// should have been modified
		assertEquals(newTestData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		FutureGet futureGet4b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet4b.awaitUninterruptibly();
		assertTrue(futureGet4b.isSuccess());
		retData = futureGet4b.getData();
		// should have been modified
		assertEquals(newTestData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	/**
	 * Put of a protected entry using version keys.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	@Test
	public void testPut2() throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4838).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.createHash("based on");

		String testData = "data";
		Data data = new Data(testData).setProtectedEntry().sign(keyPair, signatureFactory);
		data.ttlSeconds(10000).basedOn(bKey);

		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).setDomainKey(dKey).setVersionKey(vKey)
				.keyPair(keyPair).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).setVersionKey(vKey)
				.start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		Data retData = futureGet1a.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		FutureGet futureGet1b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).setVersionKey(vKey)
				.start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		retData = futureGet1b.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair.getPublic(), signatureFactory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	/**
	 * Test overwriting a protected entry with a wrong key pair.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	@Test
	public void testPut3() throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4838).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// initial put using key pair 2
		String testData1 = "data1";
		Data data = new Data(testData1).setProtectedEntry().sign(keyPair1, signatureFactory);
		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).setDomainKey(dKey).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		Data retData = futureGet1a.getData();
		assertEquals(testData1, (String) retData.object());
		assertTrue(retData.verify(keyPair1.getPublic(), signatureFactory));

		FutureGet futureGet1b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		retData = futureGet1b.getData();
		assertEquals(testData1, (String) retData.object());
		assertTrue(retData.verify(keyPair1.getPublic(), signatureFactory));

		// put with wrong key
		String testData2 = "data2";
		Data data2 = new Data(testData2).setProtectedEntry().sign(keyPair2, signatureFactory);
		FuturePut futurePut2 = p2.put(lKey).setData(cKey, data2).setDomainKey(dKey).keyPair(keyPair2).start();
		futurePut2.awaitUninterruptibly();
		assertFalse(futurePut2.isSuccess());

		FutureGet futureGet2a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet2a.awaitUninterruptibly();
		assertTrue(futureGet2a.isSuccess());
		// should have been not modified
		retData = futureGet2a.getData();
		assertEquals(testData1, (String) retData.object());
		assertTrue(retData.verify(keyPair1.getPublic(), signatureFactory));

		FutureGet futureGet2b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet2b.awaitUninterruptibly();
		assertTrue(futureGet2b.isSuccess());
		// should have been not modified
		retData = futureGet2b.getData();
		assertEquals(testData1, (String) retData.object());
		assertTrue(retData.verify(keyPair1.getPublic(), signatureFactory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testRemove1() throws NoSuchAlgorithmException, IOException, InvalidKeyException,
			SignatureException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4836).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		String locationKey = "location";
		Number160 lKey = Number160.createHash(locationKey);
		String contentKey = "content";
		Number160 cKey = Number160.createHash(contentKey);

		String testData1 = "data1";
		Data data = new Data(testData1).setProtectedEntry().sign(keyPair1, signatureFactory);

		// put trough peer 1 with key pair -------------------------------------------------------

		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setContentKey(cKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		assertEquals(testData1, (String) futureGet1a.getData().object());

		FutureGet futureGet1b = p2.get(lKey).setContentKey(cKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		assertEquals(testData1, (String) futureGet1b.getData().object());

		// try to remove without key pair -------------------------------------------------------

		FutureRemove futureRemove1a = p1.remove(lKey).contentKey(cKey).start();
		futureRemove1a.awaitUninterruptibly();
		assertFalse(futureRemove1a.isSuccess());

		FutureGet futureGet2a = p1.get(lKey).setContentKey(cKey).start();
		futureGet2a.awaitUninterruptibly();
		assertTrue(futureGet2a.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2a.getData().object());

		FutureRemove futureRemove1b = p2.remove(lKey).contentKey(cKey).start();
		futureRemove1b.awaitUninterruptibly();
		assertFalse(futureRemove1b.isSuccess());

		FutureGet futureGet2b = p2.get(lKey).setContentKey(cKey).start();
		futureGet2b.awaitUninterruptibly();
		assertTrue(futureGet2b.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2b.getData().object());

		// try to remove with wrong key pair ---------------------------------------------------

		FutureRemove futureRemove2a = p1.remove(lKey).contentKey(cKey).keyPair(keyPair2).start();
		futureRemove2a.awaitUninterruptibly();
		assertFalse(futureRemove2a.isSuccess());

		FutureGet futureGet3a = p1.get(lKey).setContentKey(cKey).start();
		futureGet3a.awaitUninterruptibly();
		assertTrue(futureGet3a.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3a.getData().object());

		FutureRemove futureRemove2b = p2.remove(lKey).contentKey(cKey).keyPair(keyPair2).start();
		futureRemove2b.awaitUninterruptibly();
		assertFalse(futureRemove2b.isSuccess());

		FutureGet futureGet3b = p2.get(lKey).setContentKey(cKey).start();
		futureGet3b.awaitUninterruptibly();
		assertTrue(futureGet3b.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3b.getData().object());

		// remove with correct key pair ---------------------------------------------------------

		FutureRemove futureRemove4 = p1.remove(lKey).contentKey(cKey).keyPair(keyPair1).start();
		futureRemove4.awaitUninterruptibly();
		assertTrue(futureRemove4.isSuccess());

		FutureGet futureGet4a = p2.get(lKey).setContentKey(cKey).start();
		futureGet4a.awaitUninterruptibly();
		assertFalse(futureGet4a.isSuccess());
		// should have been removed
		assertNull(futureGet4a.getData());

		FutureGet futureGet4b = p2.get(lKey).setContentKey(cKey).start();
		futureGet4b.awaitUninterruptibly();
		assertFalse(futureGet4b.isSuccess());
		// should have been removed
		assertNull(futureGet4b.getData());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testRemove2() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4834).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();
		KeyPair keyPair = gen.generateKeyPair();

		String locationKey = "location";
		Number160 lKey = Number160.createHash(locationKey);
		String contentKey = "content";
		Number160 cKey = Number160.createHash(contentKey);

		String testData1 = "data1";
		Data data = new Data(testData1);

		// put trough peer 1 with key pair -------------------------------------------------------

		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setContentKey(cKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		assertEquals(testData1, (String) futureGet1a.getData().object());

		FutureGet futureGet1b = p2.get(lKey).setContentKey(cKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		assertEquals(testData1, (String) futureGet1b.getData().object());

		// remove with key pair ------------------------------------------------------------------

		FutureRemove futureRemove3 = p1.remove(lKey).contentKey(cKey).keyPair(keyPair).start();
		futureRemove3.awaitUninterruptibly();
		assertTrue(futureRemove3.isSuccess());

		FutureGet futureGet3 = p2.get(lKey).setContentKey(cKey).start();
		futureGet3.awaitUninterruptibly();
		assertFalse(futureGet3.isSuccess());
		// should have been removed
		assertNull(futureGet3.getData());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testRemoveFromTo1() throws NoSuchAlgorithmException, IOException, InvalidKeyException,
			SignatureException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4838).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair key1 = gen.generateKeyPair();
		KeyPair key2 = gen.generateKeyPair();

		String locationKey = "location";
		Number160 lKey = Number160.createHash(locationKey);
		String domainKey = "domain";
		Number160 dKey = Number160.createHash(domainKey);
		String contentKey = "content";
		Number160 cKey = Number160.createHash(contentKey);

		String testData1 = "data1";
		Data data = new Data(testData1).setProtectedEntry().sign(key1, signatureFactory);

		// put trough peer 1 with key pair -------------------------------------------------------

		FuturePut futurePut1 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(key1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		assertEquals(testData1, (String) futureGet1a.getData().object());

		FutureGet futureGet1b = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		assertEquals(testData1, (String) futureGet1b.getData().object());

		// try to remove without key pair using from/to -----------------------------------------

		FutureRemove futureRemove1a = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
		futureRemove1a.awaitUninterruptibly();
		assertFalse(futureRemove1a.isSuccess());

		FutureGet futureGet2a = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet2a.awaitUninterruptibly();
		assertTrue(futureGet2a.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2a.getData().object());

		FutureRemove futureRemove1b = p2.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
		futureRemove1b.awaitUninterruptibly();
		assertFalse(futureRemove1b.isSuccess());

		FutureGet futureGet2b = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet2b.awaitUninterruptibly();
		assertTrue(futureGet2b.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2b.getData().object());

		// remove with wrong key pair -----------------------------------------------------------

		FutureRemove futureRemove2a = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key2).start();
		futureRemove2a.awaitUninterruptibly();
		assertFalse(futureRemove2a.isSuccess());

		FutureGet futureGet3a = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet3a.awaitUninterruptibly();
		assertTrue(futureGet3a.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3a.getData().object());

		FutureRemove futureRemove2b = p2.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key2).start();
		futureRemove2b.awaitUninterruptibly();
		assertFalse(futureRemove2b.isSuccess());

		FutureGet futureGet3b = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet3b.awaitUninterruptibly();
		assertTrue(futureGet3b.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3b.getData().object());

		// remove with correct key pair -----------------------------------------------------------

		FutureRemove futureRemove4 = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key1).start();
		futureRemove4.awaitUninterruptibly();
		assertTrue(futureRemove4.isSuccess());

		FutureGet futureGet4a = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet4a.awaitUninterruptibly();
		// we did not find the data
		Assert.assertTrue(futureGet4a.isFailed());
		// should have been removed
		assertNull(futureGet4a.getData());

		FutureGet futureGet4b = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet4b.awaitUninterruptibly();
		// we did not find the data
		Assert.assertTrue(futureGet4b.isFailed());
		// should have been removed
		assertNull(futureGet4b.getData());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testRemoveFromTo2() throws NoSuchAlgorithmException, IOException, InvalidKeyException,
			SignatureException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4838).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair key1 = gen.generateKeyPair();

		String locationKey = "location";
		Number160 lKey = Number160.createHash(locationKey);
		String domainKey = "domain";
		Number160 dKey = Number160.createHash(domainKey);
		String contentKey = "content";
		Number160 cKey = Number160.createHash(contentKey);

		String testData1 = "data1";
		Data data = new Data(testData1).setProtectedEntry().sign(key1, signatureFactory);

		// put trough peer 1 with key pair -------------------------------------------------------

		FuturePut futurePut1 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(key1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		assertEquals(testData1, (String) futureGet1a.getData().object());

		FutureGet futureGet1b = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		assertEquals(testData1, (String) futureGet1b.getData().object());

		// remove with correct key pair -----------------------------------------------------------

		FutureRemove futureRemove4 = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key1).start();
		futureRemove4.awaitUninterruptibly();
		assertTrue(futureRemove4.isSuccess());

		FutureGet futureGet4a = p2.get(lKey).setContentKey(cKey).start();
		futureGet4a.awaitUninterruptibly();
		// we did not find the data
		Assert.assertTrue(futureGet4a.isFailed());
		// should have been removed
		assertNull(futureGet4a.getData());

		FutureGet futureGet4b = p2.get(lKey).setContentKey(cKey).start();
		futureGet4b.awaitUninterruptibly();
		// we did not find the data
		Assert.assertTrue(futureGet4b.isFailed());
		// should have been removed
		assertNull(futureGet4b.getData());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeProtectionKeyWithVersions() throws NoSuchAlgorithmException, IOException,
			ClassNotFoundException, InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4834).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();
		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// put version 1 with protection keys 1
		Number160 vKey1 = Number160.createHash("version1");
		String testDataV1 = "data1v1";
		Data data = new Data(testDataV1).setProtectedEntry().sign(keyPair1, signatureFactory);
		data.basedOn(Number160.ZERO);
		FuturePut futurePut1 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(keyPair1)
				.setVersionKey(vKey1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// put version 2 with protection keys 1
		Number160 vKey2 = Number160.createHash("version2");
		String testDataV2 = "data1v2";
		data = new Data(testDataV2).setProtectedEntry().sign(keyPair1, signatureFactory);
		data.basedOn(vKey1);
		FuturePut futurePut2 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(keyPair1)
				.setVersionKey(vKey2).start();
		futurePut2.awaitUninterruptibly();
		assertTrue(futurePut2.isSuccess());

		// // overwrite version 2 with new protection keys 2
		// data = new Data("data1v2Overwrite").setProtectedEntry().sign(keyPair2, signatureFactory);
		// data.basedOn(vKey1);
		// FuturePut futurePut2Overwrite = p1.put(lKey).setDomainKey(dKey).setData(cKey,
		// data).keyPair(keyPair1)
		// .setVersionKey(vKey2).start();
		// futurePut2Overwrite.awaitUninterruptibly();
		// assertFalse(futurePut2Overwrite.isSuccess());

		// put version 3 with (wrong) message signature (expected to fail)
		Number160 vKey3 = Number160.createHash("version3");
		data = new Data("data1v3").setProtectedEntry().sign(keyPair1, signatureFactory);
		data.basedOn(vKey2);
		FuturePut futurePut3 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(keyPair2)
				.setVersionKey(vKey3).start();
		futurePut3.awaitUninterruptibly();
		assertFalse(futurePut3.isSuccess());

		// sign the data with the new key pair, get only the meta data
		data = new Data(testDataV2).setProtectedEntry().sign(keyPair2, signatureFactory).duplicateMeta();
		data.basedOn(vKey1);
		// change protection keys, use the old protection key to sign the message
		FuturePut futurePut4 = p1.put(lKey).setDomainKey(dKey).putMeta().setData(cKey, data)
				.setVersionKey(vKey2).keyPair(keyPair1).start();
		futurePut4.awaitUninterruptibly();
		assertTrue(futurePut4.isSuccess());

		// verify the protection keys of version 1
		Data retData = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).setVersionKey(vKey1).start()
				.awaitUninterruptibly().getData();
		assertEquals(testDataV1, (String) retData.object());
		// the protection key should be protection keys 1
		assertTrue(retData.verify(keyPair1.getPublic(), signatureFactory));

		// verify the protection keys of version 2
		retData = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).setVersionKey(vKey2).start()
				.awaitUninterruptibly().getData();
		assertEquals(testDataV2, (String) retData.object());
		// the protection key should be protection keys 2
		assertTrue(retData.verify(keyPair2.getPublic(), signatureFactory));

		// add another version with the new protection key
		Number160 vKey4 = Number160.createHash("version4");
		String testDataV4 = "data1v4";
		data = new Data(testDataV4).setProtectedEntry().sign(keyPair2, signatureFactory);
		data.basedOn(vKey2);
		FuturePut futurePut5 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(keyPair2)
				.setVersionKey(vKey4).start();
		futurePut5.awaitUninterruptibly();
		assertTrue(futurePut5.isSuccess());

		// verify the protection keys of version 4
		retData = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).setVersionKey(vKey4).start()
				.awaitUninterruptibly().getData();
		assertEquals(testDataV4, (String) retData.object());
		// the protection key should be protection keys 2
		assertTrue(retData.verify(keyPair2.getPublic(), signatureFactory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	// put version 1 with protection keys 1

	// change protection keys of version 1 to protection keys 2

	// overwrite version 1 with protection keys 2

	/**
	 * This test checks the changing of the content protection key of a signed entry in the network.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void testChangeProtectionKey() throws NoSuchAlgorithmException, IOException, InvalidKeyException,
			SignatureException, ClassNotFoundException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4834).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// initial put
		String testData = "data";
		Data data = new Data(testData).setProtectedEntry().sign(keyPair1, signatureFactory);
		FuturePut futurePut1 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// verify initial put on node 1
		Data retData = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).start().awaitUninterruptibly()
				.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair1.getPublic(), signatureFactory));
		// verify initial put on node 2
		retData = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start().awaitUninterruptibly()
				.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair1.getPublic(), signatureFactory));

		// sign the data with the new key pair, get only the meta data
		data = new Data(testData).setProtectedEntry().sign(keyPair2, signatureFactory).duplicateMeta();
		// use the old protection key to sign the message
		FuturePut futurePut2 = p1.put(lKey).setDomainKey(dKey).putMeta().setData(cKey, data)
				.keyPair(keyPair1).start();
		futurePut2.awaitUninterruptibly();
		assertTrue(futurePut2.isSuccess());

		// verify protection keys change on node 1
		retData = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).start().awaitUninterruptibly()
				.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair2.getPublic(), signatureFactory));
		// verify protection keys change on node 2
		retData = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start().awaitUninterruptibly()
				.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair2.getPublic(), signatureFactory));

		// should be not possible to modify
		data = new Data().setProtectedEntry().sign(keyPair1, signatureFactory);
		FuturePut futurePut3 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(keyPair1).start();
		futurePut3.awaitUninterruptibly();
		assertFalse(futurePut3.isSuccess());

		retData = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).start().awaitUninterruptibly()
				.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair2.getPublic(), signatureFactory));

		retData = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start().awaitUninterruptibly()
				.getData();
		assertEquals(testData, (String) retData.object());
		assertTrue(retData.verify(keyPair2.getPublic(), signatureFactory));

		// modify with new protection key
		String newTestData = "new data";
		data = new Data(newTestData).setProtectedEntry().sign(keyPair2, signatureFactory);
		FuturePut futurePut4 = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).keyPair(keyPair2).start();
		futurePut4.awaitUninterruptibly();
		assertTrue(futurePut4.isSuccess());

		retData = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).start().awaitUninterruptibly()
				.getData();
		assertEquals(newTestData, (String) retData.object());
		assertTrue(retData.verify(keyPair2.getPublic(), signatureFactory));

		retData = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).start().awaitUninterruptibly()
				.getData();
		assertEquals(newTestData, (String) retData.object());
		assertTrue(retData.verify(keyPair2.getPublic(), signatureFactory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeProtectionKeyWithReusedSignature() throws NoSuchAlgorithmException, IOException,
			ClassNotFoundException, InvalidKeyException, SignatureException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");

		// create custom RSA factories
		SignatureFactory factory = new RSASignatureFactory();
		SignatureCodec codec = new RSASignatureCodec();

		// replace default signature factories
		ChannelClientConfiguration clientConfig = PeerMaker.createDefaultChannelClientConfiguration();
		clientConfig.signatureFactory(factory);
		ChannelServerConficuration serverConfig = PeerMaker.createDefaultChannelServerConfiguration();
		serverConfig.signatureFactory(factory);

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4834).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).channelClientConfiguration(clientConfig)
				.channelServerConfiguration(serverConfig).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).channelClientConfiguration(clientConfig)
				.channelServerConfiguration(serverConfig).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair keyPairOld = gen.generateKeyPair();
		KeyPair keyPairNew = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.ZERO;

		int ttl = 10;

		String testData = "data";
		Data data = new Data(testData).setProtectedEntry().sign(keyPairOld, factory);
		data.ttlSeconds(ttl).basedOn(bKey);

		// initial put of some test data
		FuturePut futurePut = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).setVersionKey(vKey)
				.keyPair(keyPairOld).start();
		futurePut.awaitUninterruptibly();
		Assert.assertTrue(futurePut.isSuccess());

		// create signature with old key pair having the data object
		byte[] signature1 = factory.sign(keyPairOld.getPrivate(), data.buffer()).encode();

		// decrypt signature to get hash of the object
		Cipher rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.DECRYPT_MODE, keyPairOld.getPublic());
		byte[] hash = rsa.doFinal(signature1);

		// encrypt hash with new key pair to get the new signature (without having the data object)
		rsa = Cipher.getInstance("RSA");
		rsa.init(Cipher.ENCRYPT_MODE, keyPairNew.getPrivate());
		byte[] signatureNew = rsa.doFinal(hash);

		// verify old content protection keys
		Data retData = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).setVersionKey(vKey).start()
				.awaitUninterruptibly().getData();
		Assert.assertTrue(retData.verify(keyPairOld.getPublic(), factory));

		// create a dummy data object for changing the content protection key through a put meta
		Data dummyData = new Data();
		dummyData.basedOn(bKey).ttlSeconds(ttl);
		// assign the reused hash from signature (don't forget to set the signed flag)
		dummyData.signature(codec.decode(signatureNew)).signed(true).duplicateMeta();
		// change content protection key through a put meta
		FuturePut futurePutMeta = p1.put(lKey).setDomainKey(dKey).putMeta().setData(cKey, dummyData)
				.setVersionKey(vKey).keyPair(keyPairOld).start();
		futurePutMeta.awaitUninterruptibly();
		Assert.assertTrue(futurePutMeta.isSuccess());

		// verify new content protection keys
		retData = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).setVersionKey(vKey).start()
				.awaitUninterruptibly().getData();
		Assert.assertTrue(retData.verify(keyPairNew.getPublic(), factory));

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeProtectionKey2() throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4838).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();
		KeyPair keyPair2 = gen.generateKeyPair();
		KeyPair keyPair3 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash(2); // same like node 2
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		// initial put with protection key 1
		Data data1 = new Data("data1").setProtectedEntry();
		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data1).setDomainKey(dKey).keyPair(keyPair1).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// verify initial put
		assertEquals("data1", (String) p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start()
				.awaitUninterruptibly().getData().object());

		// overwrite with protection key 1
		Data data2 = new Data("data2").setProtectedEntry();
		FuturePut futurePut2 = p1.put(lKey).setData(cKey, data2).setDomainKey(dKey).keyPair(keyPair1).start();
		futurePut2.awaitUninterruptibly();
		assertTrue(futurePut2.isSuccess());

		// verify overwrite
		assertEquals("data2", (String) p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start()
				.awaitUninterruptibly().getData().object());

		// try to overwrite without protection key (expected to fail)
		Data data2A = new Data("data2A").setProtectedEntry();
		FuturePut futurePut2A = p1.put(lKey).setData(cKey, data2A).setDomainKey(dKey).start();
		futurePut2A.awaitUninterruptibly();
		assertFalse(futurePut2A.isSuccess());

		// verify that nothing changed
		assertEquals("data2", (String) p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start()
				.awaitUninterruptibly().getData().object());

		// try to overwrite with wrong protection key 2 (expected to fail)
		Data data2B = new Data("data2B").setProtectedEntry();
		FuturePut futurePut2B = p1.put(lKey).setData(cKey, data2B).setDomainKey(dKey).keyPair(keyPair2)
				.start();
		futurePut2B.awaitUninterruptibly();
		assertFalse(futurePut2A.isSuccess());

		// verify that nothing changed
		assertEquals("data2", (String) p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start()
				.awaitUninterruptibly().getData().object());

		// change protection key, create meta data
		Data data3 = new Data().setProtectedEntry().publicKey(keyPair2.getPublic()).duplicateMeta();
		// change protection keys, use the old protection key 1 to sign the message
		FuturePut futurePut3 = p1.put(lKey).setDomainKey(dKey).putMeta().setData(cKey, data3)
				.keyPair(keyPair1).start();
		futurePut3.awaitUninterruptibly();
		assertTrue(futurePut3.isSuccess());
		
		// overwrite with protection key 2
		Data data4 = new Data("data4").setProtectedEntry();
		FuturePut futurePut4 = p1.put(lKey).setData(cKey, data4).setDomainKey(dKey).keyPair(keyPair2).start();
		futurePut4.awaitUninterruptibly();
		assertTrue(futurePut4.isSuccess());

		// verify overwrite
		assertEquals("data4", (String) p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start()
				.awaitUninterruptibly().getData().object());

		// try to overwrite without protection key (expected to fail)
		Data data5A = new Data("data5A").setProtectedEntry();
		FuturePut futurePut5A = p1.put(lKey).setData(cKey, data5A).setDomainKey(dKey).start();
		futurePut5A.awaitUninterruptibly();
		assertFalse(futurePut5A.isSuccess());
		
		// verify that nothing changed
		assertEquals("data4", (String) p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start()
				.awaitUninterruptibly().getData().object());

		// try to overwrite with wrong protection key 3 (expected to fail)
		Data data5B = new Data("data5B").setProtectedEntry();
		FuturePut futurePut5B = p1.put(lKey).setData(cKey, data5B).setDomainKey(dKey).keyPair(keyPair3)
				.start();
		futurePut5B.awaitUninterruptibly();
		assertFalse(futurePut5A.isSuccess());
		
		// verify that nothing changed
		assertEquals("data4", (String) p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start()
				.awaitUninterruptibly().getData().object());
		
		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}
	
	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
