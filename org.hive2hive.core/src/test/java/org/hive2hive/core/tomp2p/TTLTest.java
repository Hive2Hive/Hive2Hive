package org.hive2hive.core.tomp2p;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import net.tomp2p.futures.FutureDigest;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
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
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4834).keyPair(keyPairPeer1)
				.setEnableIndirectReplication(true).storageIntervalMillis(1).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1).keyPair(keyPairPeer2)
				.setEnableIndirectReplication(true).storageIntervalMillis(1).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		KeyPair keyPair1 = gen.generateKeyPair();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");
		Number160 vKey = Number160.createHash("version");
		Number160 bKey = Number160.ZERO;

		int ttl = 4;

		String testData = "data";
		Data data = new Data(testData).setProtectedEntry();
		data.ttlSeconds(ttl).basedOn(bKey);

		// initial put
		FuturePut futurePut = p1.put(lKey).setDomainKey(dKey).setData(cKey, data).setVersionKey(vKey)
				.keyPair(keyPair1).start();
		futurePut.awaitUninterruptibly();
		Assert.assertTrue(futurePut.isSuccess());

		// wait a moment, so that the ttl decrements
		Thread.sleep(2000);

		// check decrement of ttl through a normal get
		FutureGet futureGet = p1.get(lKey).setDomainKey(dKey).setContentKey(cKey).setVersionKey(vKey).start();
		futureGet.awaitUninterruptibly();
		Assert.assertTrue(futureGet.isSuccess());
		Assert.assertTrue(ttl > futureGet.getData().ttlSeconds());

		// check decrement of ttl through a get meta
		FutureDigest futureDigest = p1.digest(lKey).setDomainKey(dKey).setContentKey(cKey)
				.setVersionKey(vKey).returnMetaValues().start();
		futureDigest.awaitUninterruptibly();
		Assert.assertTrue(futureDigest.isSuccess());
		Data dataMeta = futureDigest.getDigest().dataMap().values().iterator().next();
		Assert.assertTrue(ttl > dataMeta.ttlSeconds());

		// wait again a moment, till data gets expired
		Thread.sleep(2000);

		// check if data has been removed
		Data retData = p2.get(lKey).setDomainKey(dKey).setContentKey(cKey).setVersionKey(vKey).start()
				.awaitUninterruptibly().getData();
		Assert.assertNull(retData);

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
