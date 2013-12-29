package org.hive2hive.core.test.network.data;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IGetListener;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.H2HTestData;
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
	private static final int NUM_OF_THREADS = 100;
	private static final String locationKey = NetworkTestUtil.randomString();
	private static final String contentKey = NetworkTestUtil.randomString();

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = DataManagerConcurrencyTest.class;
		beforeClass();
		network = NetworkTestUtil.createNetwork(networkSize);

		// put some content first
		final CountDownLatch latch = new CountDownLatch(1);
		IPutListener listener = new IPutListener() {

			@Override
			public void onPutSuccess() {
				latch.countDown();
			}

			@Override
			public void onPutFailure() {
				Assert.fail();
			}
		};

		network.get(random.nextInt(networkSize)).getDataManager()
				.put(locationKey, contentKey, new H2HTestData(NetworkTestUtil.randomString()), listener);
		latch.await();
	}

	@Test
	public void testConcurrentPutGet() throws InterruptedException {
		// counting listener for multiple threads
		final AtomicInteger counter = new AtomicInteger();

		// start multiple threads to perform a get
		ExecutorService taskExecutor = Executors.newFixedThreadPool(NUM_OF_THREADS);
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			// take getter randomly
			NetworkManager getter = network.get(random.nextInt(networkSize));
			taskExecutor.execute(new SleepyGetRunnable(counter, getter));
		}

		taskExecutor.shutdown();
		taskExecutor.awaitTermination(60, TimeUnit.SECONDS);

		Assert.assertEquals(NUM_OF_THREADS, counter.get());
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
	private class SleepyGetRunnable implements Runnable {

		private final Object waiter = new Object();
		private final AtomicInteger counter;
		private final NetworkManager getter;

		public SleepyGetRunnable(AtomicInteger counter, NetworkManager getter) {
			this.counter = counter;
			this.getter = getter;
		}

		@Override
		public void run() {
			IGetListener listener = new IGetListener() {

				@Override
				public void handleGetResult(NetworkContent content) {
					if (content == null) {
						Assert.fail("Got null result but not expected");
					} else {
						// content is ok
						counter.incrementAndGet();
					}

					try {
						synchronized (waiter) {
							// provocate netty by sleeping here (between 1s and 6s)
							waiter.wait(1000 + random.nextInt(5000));
						}
					} catch (InterruptedException e) {
						// ignore
						e.printStackTrace();
					}
				}
			};

			// sleep that I start at random time
			try {
				synchronized (waiter) {
					waiter.wait(new Random().nextInt(300));
				}
			} catch (InterruptedException e) {
				// ignore and start the get
				e.printStackTrace();
			}

			// get the test data
			getter.getDataManager().get(locationKey, contentKey, listener);
		}
	}
}
