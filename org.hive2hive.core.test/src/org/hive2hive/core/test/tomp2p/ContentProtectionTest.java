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
import java.util.UUID;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
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

		String locationKey = "location";
		Number160 lKey = Number160.createHash(locationKey);
		String domainKey = "domain";
		Number160 dKey = Number160.createHash(domainKey);
		String contentKey = "content";
		Number160 cKey = Number160.createHash(contentKey);

		String testData1 = "data1";
		Data data = new Data(testData1).setProtectedEntry().sign(keyPair);

		// put trough peer 1 with key pair -------------------------------------------------------

		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).setDomainKey(dKey).keyPair(keyPair).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		assertEquals(testData1, (String) futureGet1a.getData().object());

		FutureGet futureGet1b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		assertEquals(testData1, (String) futureGet1b.getData().object());

		// put trough peer 2 without key pair ----------------------------------------------------

		String testData2 = "data2";
		Data data2 = new Data(testData2);
		FuturePut futurePut2 = p2.put(lKey).setData(cKey, data2).setDomainKey(dKey).start();
		futurePut2.awaitUninterruptibly();
		assertFalse(futurePut2.isSuccess());

		FutureGet futureGet2 = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet2.awaitUninterruptibly();
		assertTrue(futureGet2.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2.getData().object());

		// put trough peer 1 without key pair ----------------------------------------------------

		String testData3 = "data3";
		Data data3 = new Data(testData3);
		FuturePut futurePut3 = p2.put(lKey).setData(cKey, data3).setDomainKey(dKey).start();
		futurePut3.awaitUninterruptibly();
		assertFalse(futurePut3.isSuccess());

		FutureGet futureGet3 = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet3.awaitUninterruptibly();
		assertTrue(futureGet3.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3.getData().object());

		// now we put a signed data object -------------------------------------------------------
		data = new Data("Juhuu").setProtectedEntry().sign(keyPair);
		FuturePut futurePut4 = p1.put(lKey).setData(cKey, data).keyPair(keyPair).setDomainKey(dKey).start();
		futurePut4.awaitUninterruptibly();
		Assert.assertTrue(futurePut4.isSuccess());

		FutureGet futureGet4 = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureGet4.awaitUninterruptibly();
		Assert.assertTrue(futureGet4.isSuccess());
		Assert.assertEquals("Juhuu", (String) futureGet4.getData().object());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

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

		String locationKey = "location";
		Number160 lKey = Number160.createHash(locationKey);
		String domainKey = "domain";
		Number160 dKey = Number160.createHash(domainKey);
		String contentKey = "content";
		Number160 cKey = Number160.createHash(contentKey);
		String versionKey = "version";
		Number160 vKey = Number160.createHash(versionKey);

		String testData1 = "data1";
		Data data = new Data(testData1).setProtectedEntry().sign(keyPair);
		data.ttlSeconds(10000).basedOn(Number160.createHash("based on"));

		// put trough peer 1 with key pair -------------------------------------------------------

		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).setDomainKey(dKey).setVersionKey(vKey)
				.keyPair(keyPair).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		FutureGet futureGet1a = p1.get(lKey).setContentKey(cKey).setDomainKey(dKey).setVersionKey(vKey)
				.start();
		futureGet1a.awaitUninterruptibly();
		assertTrue(futureGet1a.isSuccess());
		assertEquals(testData1, (String) futureGet1a.getData().object());

		FutureGet futureGet1b = p2.get(lKey).setContentKey(cKey).setDomainKey(dKey).setVersionKey(vKey)
				.start();
		futureGet1b.awaitUninterruptibly();
		assertTrue(futureGet1b.isSuccess());
		assertEquals(testData1, (String) futureGet1b.getData().object());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

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
		KeyPair keyPair = gen.generateKeyPair();

		String locationKey = "location";
		Number160 lKey = Number160.createHash(locationKey);
		String domainKey = "domain";
		Number160 dKey = Number160.createHash(domainKey);
		String contentKey = "content";
		Number160 cKey = Number160.createHash(contentKey);
		String versionKey = "version";
		Number160 vKey = Number160.createHash(versionKey);
		String basedOnKey = "based on";
		Number160 bKey = Number160.createHash(basedOnKey);

		H2HTestData testData = new H2HTestData(UUID.randomUUID().toString());

		Data data = new Data(testData);
		data.ttlSeconds(10000);
		data.basedOn(bKey);
		data.setProtectedEntry().sign(keyPair);
		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).setDomainKey(dKey).setVersionKey(vKey)
				.keyPair(keyPair).start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

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
		Data data = new Data(testData1).setProtectedEntry().sign(keyPair1);

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

		FutureRemove futureRemove2b = p2.remove(lKey).contentKey(cKey).start();
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
	public void testRemoveFromTo() throws NoSuchAlgorithmException, IOException, InvalidKeyException,
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
		Data data = new Data(testData1).setProtectedEntry().sign(key1);

		// put trough peer 1 with key pair -------------------------------------------------------

		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).keyPair(key1).start();
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

		// try to remove without key pair using from/to -----------------------------------------

		FutureRemove futureRemove1a = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
		futureRemove1a.awaitUninterruptibly();
		assertFalse(futureRemove1a.isSuccess());

		FutureGet futureGet2a = p1.get(lKey).setContentKey(cKey).start();
		futureGet2a.awaitUninterruptibly();
		assertTrue(futureGet2a.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2a.getData().object());

		FutureRemove futureRemove1b = p2.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
		futureRemove1b.awaitUninterruptibly();
		assertFalse(futureRemove1b.isSuccess());

		FutureGet futureGet2b = p2.get(lKey).setContentKey(cKey).start();
		futureGet2b.awaitUninterruptibly();
		assertTrue(futureGet2b.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet2b.getData().object());

		// remove with wrong key pair -----------------------------------------------------------

		FutureRemove futureRemove2a = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key2).start();
		futureRemove2a.awaitUninterruptibly();
		assertFalse(futureRemove2a.isSuccess());

		FutureGet futureGet3a = p2.get(lKey).setContentKey(cKey).start();
		futureGet3a.awaitUninterruptibly();
		assertTrue(futureGet3a.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3a.getData().object());

		FutureRemove futureRemove2b = p2.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key2).start();
		futureRemove2b.awaitUninterruptibly();
		assertFalse(futureRemove2b.isSuccess());

		FutureGet futureGet3b = p2.get(lKey).setContentKey(cKey).start();
		futureGet3b.awaitUninterruptibly();
		assertTrue(futureGet3b.isSuccess());
		// should have been not modified
		assertEquals(testData1, (String) futureGet3b.getData().object());

		// remove with correct key pair -----------------------------------------------------------

		FutureRemove futureRemove4 = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).keyPair(key1).start();
		futureRemove4.awaitUninterruptibly();
		assertTrue(futureRemove4.isSuccess());

		FutureGet futureGet4a = p2.get(lKey).setContentKey(cKey).start();
		futureGet4a.awaitUninterruptibly();
		assertTrue(futureGet4a.isSuccess());
		// should have been removed
		assertNull(futureGet4a.getData());

		FutureGet futureGet4b = p2.get(lKey).setContentKey(cKey).start();
		futureGet4b.awaitUninterruptibly();
		assertTrue(futureGet4b.isSuccess());
		// should have been removed
		assertNull(futureGet4b.getData());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void testChangeProtectionKeyMultipleVersions() throws NoSuchAlgorithmException, IOException,
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
		Number160 cKey = Number160.createHash("content");

		// put the first version of the content with key pair 1
		Number160 vKey1 = Number160.createHash("version1");
		Data data = new Data("data1v1").setProtectedEntry().sign(keyPair1);
		data.basedOn(Number160.ZERO);

		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).keyPair(keyPair1).setVersionKey(vKey1)
				.start();
		futurePut1.awaitUninterruptibly();
		assertTrue(futurePut1.isSuccess());

		// add another version with the correct key pair 1
		Number160 vKey2 = Number160.createHash("version2");
		data = new Data("data1v2").setProtectedEntry().sign(keyPair1);
		data.basedOn(vKey1);

		FuturePut futurePut2 = p1.put(lKey).setData(cKey, data).keyPair(keyPair1).setVersionKey(vKey2)
				.start();
		futurePut2.awaitUninterruptibly();
		assertTrue(futurePut2.isSuccess());

		// put new version with other key pair 2 (expected to fail)
		Number160 vKey3 = Number160.createHash("version3");
		data = new Data("data1v3").setProtectedEntry().sign(keyPair2);
		data.basedOn(vKey2);

		FuturePut futurePut3 = p1.put(lKey).setData(cKey, data).keyPair(keyPair2).setVersionKey(vKey3)
				.start();
		futurePut3.awaitUninterruptibly();
		assertFalse(futurePut3.isSuccess());
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
