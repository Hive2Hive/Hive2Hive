package org.hive2hive.processframework.util;

import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.Assert;

public final class TestExecutionUtil {

	public static final int DEFAULT_WAITING_TIME = 1000;
	public static final int MAX_PROCESS_WAIT_TIME = 120;

	public static void waitDefault() {
		wait(DEFAULT_WAITING_TIME);
	}

	public static void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void waitTillSucceded(TestProcessComponentListener listener, int maxSeconds) {
		H2HWaiter waiter = new H2HWaiter(maxSeconds);
		do {
			if (listener.hasFailed())
				Assert.fail();
			waiter.tickASecond();
		} while (!listener.hasSucceeded());
	}

	public static void waitTillFailed(TestProcessComponentListener listener, int maxSeconds) {
		H2HWaiter waiter = new H2HWaiter(maxSeconds);
		do {
			if (listener.hasSucceeded())
				Assert.fail();
			waiter.tickASecond();
		} while (!listener.hasFailed());
	}

	/**
	 * Executes a process and waits until it's done. This is a simple helper method to reduce code
	 * clones.
	 */
	public static void executeProcess(IProcessComponent process) {
		executeProcessTillSucceded(process);
	}

	public static void executeProcessTillSucceded(IProcessComponent process) {
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		try {
			process.start();
			waitTillSucceded(listener, MAX_PROCESS_WAIT_TIME);
		} catch (InvalidProcessStateException e) {
			System.out.println("ERROR: Cannot wait until process is done.");
			Assert.fail();
		}
	}

	public static void executeProcessTillFailed(IProcessComponent process) {
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		try {
			process.start();
			waitTillFailed(listener, MAX_PROCESS_WAIT_TIME);
		} catch (InvalidProcessStateException e) {
			System.out.println("ERROR: Cannot wait until process is done.");
		}
	}
}
