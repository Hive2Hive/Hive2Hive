package org.hive2hive.core.test.tomp2p;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FuturePut;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageLayer;

import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
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
	 */
	@Test
	public void test() throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
		KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");

		KeyPair keyPairPeer1 = gen.generateKeyPair();
		Peer p1 = new PeerMaker(Number160.createHash(1)).ports(4838)
				.keyPair(keyPairPeer1).makeAndListen();
		KeyPair keyPairPeer2 = gen.generateKeyPair();
		Peer p2 = new PeerMaker(Number160.createHash(2)).masterPeer(p1)
				.keyPair(keyPairPeer2).makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
        p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();
		KeyPair keyPair = gen.generateKeyPair();

		String locationKey = "location";
		Number160 lKey = Number160.createHash(locationKey);
		String contentKey = "content";
		Number160 cKey = Number160.createHash(contentKey);

		String testData1 = "data1";
		Data data = new Data(testData1).setProtectedEntry();

		// put trough peer 1 with key pair -------------------------------------------------------
		
		FuturePut futurePut1 = p1.put(lKey).setData(cKey, data).keyPair(keyPair).start();
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

		// put trough peer 2 without key pair ----------------------------------------------------

		String testData2 = "data2";
		Data data2 = new Data(testData2);
		FuturePut futurePut2 = p2.put(lKey).setData(cKey, data2).start();
		futurePut2.awaitUninterruptibly();
		assertFalse(futurePut2.isSuccess());

		FutureGet futureGet2 = p2.get(lKey).setContentKey(cKey).start();
		futureGet2.awaitUninterruptibly();
		assertTrue(futureGet2.isSuccess());
		// should have been not modified
		assertEquals(testData2, (String) futureGet2.getData().object());

		// put trough peer 1 without key pair ----------------------------------------------------

		String testData3 = "data3";
		Data data3 = new Data(testData3);
		FuturePut futurePut3 = p2.put(lKey).setData(cKey, data3).start();
		futurePut3.awaitUninterruptibly();
		assertFalse(futurePut3.isSuccess());

		FutureGet futureGet3 = p2.get(lKey).setContentKey(cKey).start();
		futureGet3.awaitUninterruptibly();
		assertTrue(futureGet3.isSuccess());
		// should have been not modified ---> why it has been modified without giving a key pair?
		assertEquals(testData1, (String) futureGet3.getData().object());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	private void printPutStatus(FuturePut futurePut) {
		for (PeerAddress peeradress : futurePut.getRawResult().keySet()) {
			Map<Number640, Byte> map = futurePut.getRawResult().get(peeradress);
			if (map == null) {
				logger.warn("Empty raw result");
			} else {
				for (Number640 key : futurePut.getRawResult().get(peeradress).keySet()) {
					byte status = futurePut.getRawResult().get(peeradress).get(key);
					switch (StorageLayer.PutStatus.values()[status]) {
						case OK:
							logger.warn(StorageLayer.PutStatus.values()[status]);
							break;
						case FAILED:
						case FAILED_NOT_ABSENT:
						case FAILED_SECURITY:
						case VERSION_CONFLICT:
							logger.warn(StorageLayer.PutStatus.values()[status]);
							break;
					}
				}
			}
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
