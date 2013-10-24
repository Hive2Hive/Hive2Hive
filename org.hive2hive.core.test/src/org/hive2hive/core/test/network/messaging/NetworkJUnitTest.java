package org.hive2hive.core.test.network.messaging;

import static org.junit.Assert.fail;

import org.hive2hive.core.test.H2HJUnitTest;

public class NetworkJUnitTest extends H2HJUnitTest {

	class Waiter {
		private int counter = 0;
		private final int maxAmoutOfTicks;

		public Waiter(int anAmountOfAcceptableTicks) {
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

}
