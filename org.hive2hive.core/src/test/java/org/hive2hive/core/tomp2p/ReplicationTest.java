package org.hive2hive.core.tomp2p;

import java.io.IOException;

import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.processframework.util.H2HWaiter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ReplicationTest extends H2HJUnitTest {

	public ReplicationTest() throws Exception {
		super();
	}

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ReplicationTest.class;
		beforeClass();
	}

	@Test
	public void testReplicationPureTomP2P() throws IOException, InterruptedException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();
		PeerDHT p3 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		IndirectReplication rep1 = new IndirectReplication(p1).start();
		IndirectReplication rep2 = new IndirectReplication(p2).start();
		IndirectReplication rep3 = new IndirectReplication(p3).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();

		FuturePut putFuture = p2.put(Number160.createHash("key")).data(Number160.ZERO, Number160.ZERO, new Data("test"))
				.start();
		putFuture.awaitUninterruptibly();
		putFuture.futureRequests().awaitUninterruptibly();
		Assert.assertEquals(2, putFuture.futureRequests().successCounter());

		p3.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();

		H2HWaiter w = new H2HWaiter(10);
		Data tmp = null;
		do {
			w.tickASecond();
			tmp = p3.storageLayer().get(
					new Number640(Number160.createHash("key"), Number160.ZERO, Number160.ZERO, Number160.ZERO));
		} while (tmp == null);

		Assert.assertNotNull(tmp);

		rep1.shutdown();
		rep2.shutdown();
		rep3.shutdown();

		p1.shutdown();
		p2.shutdown();
		p3.shutdown();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
