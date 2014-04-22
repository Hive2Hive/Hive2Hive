package org.hive2hive.core.tomp2p;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.tomp2p.futures.FutureDigest;
import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FutureRemove;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerMaker;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.hive2hive.core.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test which tests the range mechanisms (from/to) of the <code>TomP2P</code> project. Tests should be completely
 * independent of <code>Hive2Hive</code>.
 * 
 * @author Seppi
 */
public class FromToTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FromToTest.class;
		beforeClass();
	}

	@Test
	public void getFromToTest1() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(4838)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++) {
			H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
			data.generateVersionKey();
			if (i > 0) {
				data.setBasedOnKey(content.get(i - 1).getVersionKey());
			}
			content.add(data);

			p2.put(Number160.createHash(locationKey))
					.setData(Number160.createHash(contentKey), new Data(data))
					.setVersionKey(data.getVersionKey()).start().awaitUninterruptibly();
		}

		FutureGet future = p1
				.get(Number160.createHash(locationKey))
				.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.ZERO))
				.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.MAX_VALUE)).descending().returnNr(1).start();
		future.awaitUninterruptibly();

		assertEquals(content.get(numberOfContent - 1).getTestString(), ((H2HTestData) future.getData()
				.object()).getTestString());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void getFromToTest2() throws IOException, ClassNotFoundException, InterruptedException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(4838)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");

		List<Long> timeStamps = new ArrayList<Long>();
		for (int i = 0; i < 5; i++) {
			long timeStamp = new Date().getTime();
			timeStamps.add(timeStamp);
			// to guarantee different time stamps
			Thread.sleep(10);
		}

		// shuffle to change the order for put
		List<Long> shuffledTimeStamps = new ArrayList<Long>(timeStamps);
		Collections.shuffle(shuffledTimeStamps);
		for (Long timeStamp : shuffledTimeStamps) {
			Number160 contentKey = new Number160(timeStamp);
			logger.debug("{}, {}", timeStamp, contentKey);
			p2.put(lKey).setData(contentKey, new Data(timeStamp)).setDomainKey(dKey).start()
					.awaitUninterruptibly();
		}

		// fetch time stamps from network, respectively the implicit queue
		List<Long> downloadedTimestamps = new ArrayList<Long>();
		while (true) {
			FutureGet futureGet = p1.get(lKey)
					.from(new Number640(lKey, dKey, Number160.ZERO, Number160.ZERO))
					.to(new Number640(lKey, dKey, Number160.MAX_VALUE, Number160.MAX_VALUE)).ascending()
					.returnNr(1).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.getData() != null) {
				long timeStamp = (Long) futureGet.getData().object();
				Number160 contentKey = new Number160(timeStamp);
				logger.debug("{}, {}", timeStamp, contentKey);
				downloadedTimestamps.add(timeStamp);
				// remove fetched time stamp from network
				p2.remove(lKey).setDomainKey(dKey).contentKey(contentKey).start().awaitUninterruptibly();
			} else {
				break;
			}
		}

		// order of fetched tasks should be like the inital one
		assertEquals(timeStamps.size(), downloadedTimestamps.size());
		for (int i = 0; i < timeStamps.size(); i++) {
			assertEquals(timeStamps.get(i), downloadedTimestamps.get(i));
		}

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void removeFromToTest1() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++) {
			H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
			data.generateVersionKey();
			if (i > 0) {
				data.setBasedOnKey(content.get(i - 1).getVersionKey());
			}
			content.add(data);

			p2.put(Number160.createHash(locationKey))
					.setData(Number160.createHash(contentKey), new Data(data))
					.setVersionKey(data.getVersionKey()).start().awaitUninterruptibly();
		}

		FutureRemove futureRemove = p1
				.remove(Number160.createHash(locationKey))
				.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.ZERO))
				.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.MAX_VALUE)).start();
		futureRemove.awaitUninterruptibly();

		FutureGet futureGet = p1
				.get(Number160.createHash(locationKey))
				.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.ZERO))
				.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.MAX_VALUE)).start();
		futureGet.awaitUninterruptibly();

		assertNull(futureGet.getData());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void removeFromToTest2() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++) {
			H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
			data.generateVersionKey();
			if (i > 0) {
				data.setBasedOnKey(content.get(i - 1).getVersionKey());
			}
			content.add(data);

			p2.put(Number160.createHash(locationKey))
					.setData(Number160.createHash(contentKey), new Data(data))
					.setVersionKey(data.getVersionKey()).start().awaitUninterruptibly();
		}

		FutureRemove futureRemove = p1
				.remove(Number160.createHash(locationKey))
				.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.ZERO))
				.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.MAX_VALUE)).start();
		futureRemove.awaitUninterruptibly();

		FutureDigest futureDigest = p1
				.digest(Number160.createHash(locationKey))
				.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.ZERO))
				.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.MAX_VALUE)).start();
		futureDigest.awaitUninterruptibly();

		assertTrue(futureDigest.getDigest().keyDigest().isEmpty());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void removeTest() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		data.generateVersionKey();

		p2.put(Number160.createHash(locationKey)).setData(Number160.createHash(contentKey), new Data(data))
				.setVersionKey(data.getVersionKey()).start().awaitUninterruptibly();

		FutureRemove futureRemove = p1.remove(Number160.createHash(locationKey)).setDomainKey(Number160.ZERO)
				.contentKey(Number160.createHash(contentKey)).setVersionKey(data.getVersionKey()).start();
		futureRemove.awaitUninterruptibly();

		FutureDigest futureDigest = p1.digest(Number160.createHash(locationKey))
				.setContentKey(Number160.createHash(contentKey)).setVersionKey(data.getVersionKey()).start();
		futureDigest.awaitUninterruptibly();

		assertTrue(futureDigest.getDigest().keyDigest().isEmpty());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void removeFromToTest3() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());

		p2.put(lKey).setData(cKey, new Data(data)).setDomainKey(dKey).start().awaitUninterruptibly();

		FutureRemove futureRemove = p1.remove(lKey).setDomainKey(dKey).contentKey(cKey).start();
		futureRemove.awaitUninterruptibly();

		// check with a normal digest
		FutureDigest futureDigest = p1.digest(lKey).setContentKey(cKey).setDomainKey(dKey).start();
		futureDigest.awaitUninterruptibly();
		assertTrue(futureDigest.getDigest().keyDigest().isEmpty());

		// check with a from/to digest
		futureDigest = p1.digest(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
		futureDigest.awaitUninterruptibly();
		assertTrue(futureDigest.getDigest().keyDigest().isEmpty());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void removeFromToTest4() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();
		p1.bootstrap().setPeerAddress(p2.getPeerAddress()).start().awaitUninterruptibly();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());

		p2.put(lKey).setData(cKey, new Data(data)).setDomainKey(dKey).start().awaitUninterruptibly();

		FutureRemove futureRemove = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
		futureRemove.awaitUninterruptibly();

		FutureDigest futureDigest = p1.digest(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
				.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
		futureDigest.awaitUninterruptibly();

		// should be empty
		assertTrue(futureDigest.getDigest().keyDigest().isEmpty());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void digestFromToTest() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++) {
			H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
			data.generateVersionKey();
			if (i > 0) {
				data.setBasedOnKey(content.get(i - 1).getVersionKey());
			}
			content.add(data);

			p2.put(Number160.createHash(locationKey))
					.setData(Number160.createHash(contentKey), new Data(data))
					.setVersionKey(data.getVersionKey()).start().awaitUninterruptibly();
		}

		FutureDigest futureDigest = p1
				.digest(Number160.createHash(locationKey))
				.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.ZERO))
				.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
						.createHash(contentKey), Number160.MAX_VALUE)).start();
		futureDigest.awaitUninterruptibly();

		assertEquals(numberOfContent, futureDigest.getDigest().keyDigest().size());

		for (H2HTestData data : content) {
			assertTrue(futureDigest
					.getDigest()
					.keyDigest()
					.containsKey(
							new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
									.createHash(contentKey), data.getVersionKey())));
		}

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void digestTest() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		List<H2HTestData> content = new ArrayList<H2HTestData>();
		int numberOfContent = 3;
		for (int i = 0; i < numberOfContent; i++) {
			H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
			data.generateVersionKey();
			if (i > 0) {
				data.setBasedOnKey(content.get(i - 1).getVersionKey());
			}
			content.add(data);

			p2.put(Number160.createHash(locationKey))
					.setData(Number160.createHash(contentKey), new Data(data))
					.setVersionKey(data.getVersionKey()).start().awaitUninterruptibly();
		}

		for (H2HTestData data : content) {
			FutureDigest future = p1.digest(Number160.createHash(locationKey)).setDomainKey(Number160.ZERO)
					.setContentKey(Number160.createHash(contentKey)).setVersionKey(data.getVersionKey())
					.start();
			future.awaitUninterruptibly();

			assertEquals(1, future.getDigest().keyDigest().size());
			assertEquals(
					new Number640(Number160.createHash(locationKey), Number160.ZERO,
							Number160.createHash(contentKey), data.getVersionKey()), future.getDigest()
							.keyDigest().firstKey());
		}

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@Test
	public void putTest() throws IOException, ClassNotFoundException {
		Peer p1 = new PeerMaker(Number160.createHash(1)).setEnableIndirectReplication(true).ports(5000)
				.makeAndListen();
		Peer p2 = new PeerMaker(Number160.createHash(2)).setEnableIndirectReplication(true).masterPeer(p1)
				.makeAndListen();

		p2.bootstrap().setPeerAddress(p1.getPeerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		H2HTestData data = new H2HTestData(NetworkTestUtil.randomString());
		data.generateVersionKey();
		// data.setBasedOnKey(Number160.createHash(10));

		p2.put(Number160.createHash(locationKey)).setData(Number160.createHash(contentKey), new Data(data))
				.setVersionKey(data.getVersionKey()).start().awaitUninterruptibly();

		FutureGet futureGet = p2.get(Number160.createHash(locationKey))
				.setContentKey(Number160.createHash(contentKey)).setVersionKey(data.getVersionKey()).start();
		futureGet.awaitUninterruptibly();

		assertNotNull(futureGet.getData());

		p1.shutdown().awaitUninterruptibly();
		p2.shutdown().awaitUninterruptibly();
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
