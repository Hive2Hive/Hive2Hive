package org.hive2hive.core.tomp2p;

import java.io.IOException;

import net.tomp2p.futures.FuturePut;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
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
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();
		Peer p3 = new PeerMaker(Number160.createHash(3)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		FuturePut putFuture = p2.put(Number160.createHash("key"))
				.setData(Number160.ZERO, Number160.ZERO, new Data("test")).start();
		putFuture.awaitUninterruptibly();
		putFuture.getFutureRequests().awaitUninterruptibly();
		Assert.assertEquals(2, putFuture.getFutureRequests().getSuccessCounter());

		p3.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		H2HWaiter w = new H2HWaiter(10);
		Data tmp = null;
		do {
			w.tickASecond();
			tmp = p3.getPeerBean()
					.storage()
					.get(new Number640(Number160.createHash("key"), Number160.ZERO, Number160.ZERO,
							Number160.ZERO));
		} while (tmp == null);

		Assert.assertNotNull(tmp);

		p1.shutdown();
		p2.shutdown();
		p3.shutdown();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
