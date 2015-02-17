package org.hive2hive.core.tomp2p;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import net.tomp2p.dht.FutureDigest;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test which test the TTL cleanup mechanism of <code>TomP2P</code>. Tests should be completely
 * independent of <code>Hive2Hive</code>.
 * 
 * @author Seppi
 */
public class TTLTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = TTLTest.class;
		beforeClass();
	}

	@Test
	public void testTTLDecrement() throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException, InterruptedException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
				.storage(new StorageMemory(100)).start();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
				.start()).storage(new StorageMemory(100)).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.ZERO;

		int ttl = 3;

		String testData = "data";
		Data data = new Data(testData).protectEntry(keyPair1);
		data.ttlSeconds(ttl).addBasedOn(bKey);

		try {
			// initial put
			FuturePut futurePut = p1.put(lKey).domainKey(dKey).data(cKey, data).versionKey(vKey).keyPair(keyPair1).start();
			futurePut.awaitUninterruptibly();
			Assert.assertTrue(futurePut.isSuccess());

			// wait a moment, so that the ttl decrements
			Thread.sleep(2000);

			// check decrement of ttl through a normal get
			FutureGet futureGet = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start();
			futureGet.awaitUninterruptibly();
			Assert.assertTrue(futureGet.isSuccess());
			Assert.assertTrue(ttl > futureGet.data().ttlSeconds());

			// check decrement of ttl through a get meta
			FutureDigest futureDigest = p1.digest(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).returnMetaValues()
					.start();
			futureDigest.awaitUninterruptibly();
			Assert.assertTrue(futureDigest.isSuccess());
			Data dataMeta = futureDigest.digest().dataMap().values().iterator().next();
			Assert.assertTrue(ttl > dataMeta.ttlSeconds());

			// wait again a moment, till data gets expired
			Thread.sleep(2000);

			// check if data has been removed
			Data retData = p2.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(vKey).start().awaitUninterruptibly()
					.data();
			Assert.assertNull(retData);
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void testMaxVersionLimit() throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException, InterruptedException {
		PeerDHT p1 = null;
		PeerDHT p2 = null;
		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
			// create peers which accept only two versions
			KeyPair keyPairPeer1 = gen.generateKeyPair();
			p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).keyPair(keyPairPeer1).start())
					.storage(new StorageMemory(1000, 2)).start();
			KeyPair keyPairPeer2 = gen.generateKeyPair();
			p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).keyPair(keyPairPeer2)
					.start()).storage(new StorageMemory(1000, 2)).start();
			p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
			p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();
			KeyPair keyPair1 = gen.generateKeyPair();
			Number160 lKey = Number160.createHash("location");
			Number160 dKey = Number160.createHash("domain");
			Number160 cKey = Number160.createHash("content");

			// put first version
			FuturePut futurePut = p1.put(lKey).domainKey(dKey).data(cKey, new Data("version1").protectEntry(keyPair1))
					.versionKey(new Number160(0, new Number160(0))).keyPair(keyPair1).start();
			futurePut.awaitUninterruptibly();
			Assert.assertTrue(futurePut.isSuccess());
			// put second version
			futurePut = p1.put(lKey).domainKey(dKey).data(cKey, new Data("version2").protectEntry(keyPair1))
					.versionKey(new Number160(1, new Number160(0))).keyPair(keyPair1).start();
			futurePut.awaitUninterruptibly();
			Assert.assertTrue(futurePut.isSuccess());
			// put third version
			futurePut = p1.put(lKey).domainKey(dKey).data(cKey, new Data("version3").protectEntry(keyPair1))
					.versionKey(new Number160(2, new Number160(0))).keyPair(keyPair1).start();
			futurePut.awaitUninterruptibly();
			Assert.assertTrue(futurePut.isSuccess());
			// wait for maintenance to kick in
			Thread.sleep(1500);
			// first version should be not available
			FutureGet futureGet = p1.get(lKey).domainKey(dKey).contentKey(cKey).versionKey(new Number160(0)).start();
			futureGet.awaitUninterruptibly();
			Assert.assertTrue(futureGet.isSuccess());
			Assert.assertNull(futureGet.data());
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
