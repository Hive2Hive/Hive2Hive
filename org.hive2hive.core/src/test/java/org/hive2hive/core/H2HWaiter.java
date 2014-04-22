package org.hive2hive.core;

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
	private final int maxAmoutOfTicks;

	public H2HWaiter(int anAmountOfAcceptableTicks) {
		maxAmoutOfTicks = anAmountOfAcceptableTicks;
	}

	public void tickASecond() {
		synchronized (this) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
			}
		}
		counter++;
		if (counter >= maxAmoutOfTicks) {
			fail(String.format("We waited for %d seconds. This is simply to long!", counter));
		}
	}
}