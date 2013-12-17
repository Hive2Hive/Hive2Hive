package org.hive2hive.core.test.network.data;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
import org.hive2hive.core.test.H2HWaiter;
import org.hive2hive.core.test.network.NetworkTestUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Nico
 *         Test to prove that some events are not triggered using the Hive2Hive
 *         DataManager and listeners.
 */
public class DataManagerConcurrencyTest extends H2HJUnitTest {

	private static List<NetworkManager> network;
	private static final int networkSize = 10;
	private static Random random = new Random();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DataManagerConcurrencyTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);
	}

	@Test
	public void testConcurrentPutGet() {
		// counting listener for multiple threads
		final AtomicInteger counter = new AtomicInteger();

		// start multiple threads to perform a get
		final int NUM_OF_THREADS = 100;
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			// take putter and getter randomly (can be same)
			NetworkManager putter = network.get(random.nextInt(networkSize));
			NetworkManager getter = network.get(random.nextInt(networkSize));
			new Thread(new PutGetRandomRunnable(counter, putter, getter)).start();
		}

		// wait some time (20s is enough since everything happens locally)
		H2HWaiter waiter = new H2HWaiter(20);
		while (counter.get() < NUM_OF_THREADS) {
			waiter.tickASecond();
		}
	}

	@AfterClass
	public static void cleanAfterClass() {
		NetworkTestUtil.shutdownNetwork(network);
		afterClass();
	}

	/**
	 * Runnable that first puts a dummy content, then gets it (using another peer).
	 * 
	 * @author Nico
	 * 
	 */
	private class PutGetRandomRunnable implements Runnable {

		private final AtomicInteger counter;
		private final NetworkManager putter;
		private final NetworkManager getter;
		private final H2HTestData data;

		public PutGetRandomRunnable(AtomicInteger counter, NetworkManager putter, NetworkManager getter) {
			this.counter = counter;
			this.putter = putter;
			this.getter = getter;
			data = new H2HTestData(NetworkTestUtil.randomString());
		}

		@Override
		public void run() {
			final String locationKey = NetworkTestUtil.randomString();
			final String contentKey = NetworkTestUtil.randomString();

			final IPutListener putListener = new IPutListener() {

				@Override
				public void onPutSuccess() {
					// then get
					IGetListener listener = new IGetListener() {
						@Override
						public void handleGetResult(NetworkContent content) {
							if (content == null)
								Assert.fail("Content is null");
							else
								counter.incrementAndGet();
						}
					};

					try {
						Thread.sleep(new Random().nextInt(100));
					} catch (InterruptedException e) {
						// ignore and continue
					}
					getter.getDataManager().get(locationKey, contentKey, listener);
				}

				@Override
				public void onPutFailure() {
					Assert.fail("Put failed");
				}
			};

			// put first
			putter.getDataManager().put(locationKey, contentKey, data, putListener);
		}
	}
}
