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

import net.tomp2p.dht.FutureDigest;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.H2HTestData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test which tests the range mechanisms (from/to) of the <code>TomP2P</code> project. Tests should be
 * completely independent of <code>Hive2Hive</code>.
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
	public void getFromToTest1() throws IOException, ClassNotFoundException, InterruptedException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(4838).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		Number160 locationKey = Number160.createHash("location");
		Number160 contentKey = Number160.createHash("content");

		try {
			List<H2HTestData> contents = new ArrayList<H2HTestData>();
			int numberOfContent = 3;
			for (int i = 0; i < numberOfContent; i++) {
				H2HTestData data = new H2HTestData(randomString());
				contents.add(data);

				Data object = new Data(data);
				if (i == 0) {
					object.addBasedOn(Number160.ZERO);
				} else {
					object.addBasedOn(contents.get(i - 1).getVersionKey());
				}
				p2.put(locationKey).data(contentKey, object).versionKey(new Number160(i)).start().awaitUninterruptibly();
			}

			// get the last version
			Number640 from = new Number640(locationKey, Number160.ZERO, contentKey, Number160.ZERO);
			Number640 to = new Number640(locationKey, Number160.ZERO, contentKey, Number160.MAX_VALUE);
			System.err.println("from: " + from);
			System.err.println("to:   " + to);
			FutureGet future = p1.get(locationKey).from(from).to(to).descending().returnNr(1).start();
			future.awaitUninterruptibly();

			assertEquals(contents.get(numberOfContent - 1).getTestString(),
					((H2HTestData) future.data().object()).getTestString());
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void getFromToTest2() throws IOException, ClassNotFoundException, InterruptedException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(4838).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");

		List<Long> timeStamps = new ArrayList<Long>();
		for (int i = 0; i < 5; i++) {
			long timeStamp = new Date().getTime();
			timeStamps.add(timeStamp);
			// to guarantee different time stamps
			Thread.sleep(10);
		}

		try {
			// shuffle to change the order for put
			List<Long> shuffledTimeStamps = new ArrayList<Long>(timeStamps);
			Collections.shuffle(shuffledTimeStamps);
			for (Long timeStamp : shuffledTimeStamps) {
				Number160 contentKey = new Number160(timeStamp);
				logger.debug("{}, {}", timeStamp, contentKey);
				p2.put(lKey).data(contentKey, new Data(timeStamp)).domainKey(dKey).start().awaitUninterruptibly();
			}

			// fetch time stamps from network, respectively the implicit queue
			List<Long> downloadedTimestamps = new ArrayList<Long>();
			while (true) {
				FutureGet futureGet = p1.get(lKey).from(new Number640(lKey, dKey, Number160.ZERO, Number160.ZERO))
						.to(new Number640(lKey, dKey, Number160.MAX_VALUE, Number160.MAX_VALUE)).ascending().returnNr(1)
						.start();
				futureGet.awaitUninterruptibly();
				if (futureGet.data() != null) {
					long timeStamp = (Long) futureGet.data().object();
					Number160 contentKey = new Number160(timeStamp);
					logger.debug("{}, {}", timeStamp, contentKey);
					downloadedTimestamps.add(timeStamp);
					// remove fetched time stamp from network
					p2.remove(lKey).domainKey(dKey).contentKey(contentKey).start().awaitUninterruptibly();
				} else {
					break;
				}
			}

			// order of fetched tasks should be like the inital one
			assertEquals(timeStamps.size(), downloadedTimestamps.size());
			for (int i = 0; i < timeStamps.size(); i++) {
				assertEquals(timeStamps.get(i), downloadedTimestamps.get(i));
			}
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void removeFromToTest1() throws IOException, ClassNotFoundException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		try {
			List<H2HTestData> content = new ArrayList<H2HTestData>();
			int numberOfContent = 3;
			for (int i = 0; i < numberOfContent; i++) {
				H2HTestData data = new H2HTestData(randomString());
				data.generateVersionKey();
				if (i > 0) {
					data.setBasedOnKey(content.get(i - 1).getVersionKey());
				}
				content.add(data);

				p2.put(Number160.createHash(locationKey)).data(Number160.createHash(contentKey), new Data(data))
						.versionKey(data.getVersionKey()).start().awaitUninterruptibly();
			}

			FutureRemove futureRemove = p1
					.remove(Number160.createHash(locationKey))
					.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.ZERO))
					.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.MAX_VALUE)).start();
			futureRemove.awaitUninterruptibly();

			FutureGet futureGet = p1
					.get(Number160.createHash(locationKey))
					.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.ZERO))
					.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.MAX_VALUE)).start();
			futureGet.awaitUninterruptibly();

			assertNull(futureGet.data());
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void removeFromToTest2() throws IOException, ClassNotFoundException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		try {
			List<H2HTestData> content = new ArrayList<H2HTestData>();
			int numberOfContent = 3;
			for (int i = 0; i < numberOfContent; i++) {
				H2HTestData data = new H2HTestData(randomString());
				data.generateVersionKey();
				if (i > 0) {
					data.setBasedOnKey(content.get(i - 1).getVersionKey());
				}
				content.add(data);

				p2.put(Number160.createHash(locationKey)).data(Number160.createHash(contentKey), new Data(data))
						.versionKey(data.getVersionKey()).start().awaitUninterruptibly();
			}

			FutureRemove futureRemove = p1
					.remove(Number160.createHash(locationKey))
					.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.ZERO))
					.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.MAX_VALUE)).start();
			futureRemove.awaitUninterruptibly();

			FutureDigest futureDigest = p1
					.digest(Number160.createHash(locationKey))
					.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.ZERO))
					.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.MAX_VALUE)).start();
			futureDigest.awaitUninterruptibly();

			assertTrue(futureDigest.digest().keyDigest().isEmpty());
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void removeTest() throws IOException, ClassNotFoundException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		H2HTestData data = new H2HTestData(randomString());
		data.generateVersionKey();

		try {
			p2.put(Number160.createHash(locationKey)).data(Number160.createHash(contentKey), new Data(data))
					.versionKey(data.getVersionKey()).start().awaitUninterruptibly();

			FutureRemove futureRemove = p1.remove(Number160.createHash(locationKey)).domainKey(Number160.ZERO)
					.contentKey(Number160.createHash(contentKey)).versionKey(data.getVersionKey()).start();
			futureRemove.awaitUninterruptibly();

			FutureDigest futureDigest = p1.digest(Number160.createHash(locationKey))
					.contentKey(Number160.createHash(contentKey)).versionKey(data.getVersionKey()).start();
			futureDigest.awaitUninterruptibly();

			assertTrue(futureDigest.digest().keyDigest().isEmpty());
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void removeFromToTest3() throws IOException, ClassNotFoundException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		H2HTestData data = new H2HTestData(randomString());

		try {
			p2.put(lKey).data(cKey, new Data(data)).domainKey(dKey).start().awaitUninterruptibly();

			FutureRemove futureRemove = p1.remove(lKey).domainKey(dKey).contentKey(cKey).start();
			futureRemove.awaitUninterruptibly();

			// check with a normal digest
			FutureDigest futureDigest = p1.digest(lKey).contentKey(cKey).domainKey(dKey).start();
			futureDigest.awaitUninterruptibly();
			assertTrue(futureDigest.digest().keyDigest().isEmpty());

			// check with a from/to digest
			futureDigest = p1.digest(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
					.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
			futureDigest.awaitUninterruptibly();
			assertTrue(futureDigest.digest().keyDigest().isEmpty());
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void removeFromToTest4() throws IOException, ClassNotFoundException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();
		p1.peer().bootstrap().peerAddress(p2.peerAddress()).start().awaitUninterruptibly();

		Number160 lKey = Number160.createHash("location");
		Number160 dKey = Number160.createHash("domain");
		Number160 cKey = Number160.createHash("content");

		H2HTestData data = new H2HTestData(randomString());

		try {
			p2.put(lKey).data(cKey, new Data(data)).domainKey(dKey).start().awaitUninterruptibly();

			FutureRemove futureRemove = p1.remove(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
					.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
			futureRemove.awaitUninterruptibly();

			FutureDigest futureDigest = p1.digest(lKey).from(new Number640(lKey, dKey, cKey, Number160.ZERO))
					.to(new Number640(lKey, dKey, cKey, Number160.MAX_VALUE)).start();
			futureDigest.awaitUninterruptibly();

			// should be empty
			assertTrue(futureDigest.digest().keyDigest().isEmpty());
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void digestFromToTest() throws IOException, ClassNotFoundException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		try {
			List<H2HTestData> content = new ArrayList<H2HTestData>();
			int numberOfContent = 3;
			for (int i = 0; i < numberOfContent; i++) {
				H2HTestData data = new H2HTestData(randomString());
				data.generateVersionKey();
				if (i > 0) {
					data.setBasedOnKey(content.get(i - 1).getVersionKey());
				}
				content.add(data);

				p2.put(Number160.createHash(locationKey)).data(Number160.createHash(contentKey), new Data(data))
						.versionKey(data.getVersionKey()).start().awaitUninterruptibly();
			}

			FutureDigest futureDigest = p1
					.digest(Number160.createHash(locationKey))
					.from(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.ZERO))
					.to(new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
							Number160.MAX_VALUE)).start();
			futureDigest.awaitUninterruptibly();

			assertEquals(numberOfContent, futureDigest.digest().keyDigest().size());

			for (H2HTestData data : content) {
				assertTrue(futureDigest
						.digest()
						.keyDigest()
						.containsKey(
								new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160
										.createHash(contentKey), data.getVersionKey())));
			}
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void digestTest() throws IOException, ClassNotFoundException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		try {
			List<H2HTestData> content = new ArrayList<H2HTestData>();
			int numberOfContent = 3;
			for (int i = 0; i < numberOfContent; i++) {
				H2HTestData data = new H2HTestData(randomString());
				data.generateVersionKey();
				if (i > 0) {
					data.setBasedOnKey(content.get(i - 1).getVersionKey());
				}
				content.add(data);

				p2.put(Number160.createHash(locationKey)).data(Number160.createHash(contentKey), new Data(data))
						.versionKey(data.getVersionKey()).start().awaitUninterruptibly();
			}

			for (H2HTestData data : content) {
				FutureDigest future = p1.digest(Number160.createHash(locationKey)).domainKey(Number160.ZERO)
						.contentKey(Number160.createHash(contentKey)).versionKey(data.getVersionKey()).start();
				future.awaitUninterruptibly();

				assertEquals(1, future.digest().keyDigest().size());
				assertEquals(
						new Number640(Number160.createHash(locationKey), Number160.ZERO, Number160.createHash(contentKey),
								data.getVersionKey()), future.digest().keyDigest().firstKey());
			}
		} finally {
			p1.shutdown().awaitUninterruptibly();
			p2.shutdown().awaitUninterruptibly();
		}
	}

	@Test
	public void putTest() throws IOException, ClassNotFoundException {
		PeerDHT p1 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(1)).ports(5000).start()).start();
		PeerDHT p2 = new PeerBuilderDHT(new PeerBuilder(Number160.createHash(2)).masterPeer(p1.peer()).start()).start();

		p2.peer().bootstrap().peerAddress(p1.peerAddress()).start().awaitUninterruptibly();

		String locationKey = "location";
		String contentKey = "content";

		H2HTestData data = new H2HTestData(randomString());
		data.generateVersionKey();
		// data.setBasedOnKey(Number160.createHash(10));

		try {
			p2.put(Number160.createHash(locationKey)).data(Number160.createHash(contentKey), new Data(data))
					.versionKey(data.getVersionKey()).start().awaitUninterruptibly();

			FutureGet futureGet = p2.get(Number160.createHash(locationKey)).contentKey(Number160.createHash(contentKey))
					.versionKey(data.getVersionKey()).start();
			futureGet.awaitUninterruptibly();

			assertNotNull(futureGet.data());
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
