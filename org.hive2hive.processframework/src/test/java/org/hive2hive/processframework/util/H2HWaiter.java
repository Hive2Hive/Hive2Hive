package org.hive2hive.processframework.util;

import static org.junit.Assert.fail;

/**
 * A helper class for testing. Initialize the waiter with a certain amount of seconds. Use
 * {@link H2HWaiter#tickASecond()} method to sleep for a second. This method calls {@link Assert#fail()} after
 * the number of calls reaches the given amount of seconds.
 * 
 * @author Nendor
 */
public class H2HWaiter {
	private int counter = 0;
	private final int maxSeconds;

	public H2HWaiter(int maxSeconds) {
		this.maxSeconds = maxSeconds;
	}

	public void tickASecond() {
		synchronized (this) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
			}
		}
		counter++;
		if (counter >= maxSeconds) {
			fail(String.format("We waited for %s seconds. This is simply to long!", counter));
		}
	}
}