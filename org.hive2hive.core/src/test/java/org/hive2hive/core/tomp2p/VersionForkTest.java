package org.hive2hive.core.tomp2p;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Map;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageLayer.PutStatus;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class VersionForkTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = VersionForkTest.class;
		beforeClass();
	}

	@Test
	public void testVersionFork() throws Exception {
		PeerDHT p1 = null;
		PeerDHT p2 = null;

		try {
			KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
			KeyPair keyPair1 = gen.generateKeyPair();
			p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(4000).start()).start();
			p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

			p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
			p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

			Number160 locationKey = Number160.createHash(randomString());
			Number160 contentKey = Number160.createHash(randomString());

			Data versionA = new Data("versionA").addBasedOn(new Number160(0, Number160.ZERO)).protectEntry(keyPair1);
			Data versionB = new Data("versionB").addBasedOn(new Number160(0, Number160.ONE)).protectEntry(keyPair1);

			FuturePut putA = p1.put(locationKey).data(contentKey, versionA, Number160.ONE).keyPair(keyPair1).start()
					.awaitUninterruptibly();
			assertTrue(putA.isSuccess());
			assertFalse(hasVersionFork(putA));

			// put version B where a version conflict should be detected because it
			// is not based on version A
			FuturePut putB = p1.put(locationKey).data(contentKey, versionB, Number160.ONE).keyPair(keyPair1).start()
					.awaitUninterruptibly();
			assertTrue(hasVersionFork(putB));
		} finally {
			if (p1 != null) {
				p1.shutdown().awaitUninterruptibly();
			}
			if (p2 != null) {
				p2.shutdown().awaitUninterruptibly();
			}
		}
	}

	private static boolean hasVersionFork(FuturePut future) throws Exception {
		if (future.isFailed() || future.rawResult().isEmpty()) {
			throw new Exception("Future failed");
		}

		for (PeerAddress peeradress : future.rawResult().keySet()) {
			Map<Number640, Byte> map = future.rawResult().get(peeradress);
			if (map != null) {
				for (Number640 key : map.keySet()) {
					byte putStatus = map.get(key);
					if (putStatus == -1) {
						throw new Exception("Got an invalid status: " + putStatus);
					} else {
						switch (PutStatus.values()[putStatus]) {
							case VERSION_FORK:
								return true;
							default:
								break;
						}
					}
				}
			}
		}

		return false;
	}
}
