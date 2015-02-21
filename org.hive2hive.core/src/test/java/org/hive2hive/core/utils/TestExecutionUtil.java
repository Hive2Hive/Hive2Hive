package org.hive2hive.core.utils;

import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.junit.Assert;

public final class TestExecutionUtil {

	public static final int MAX_PROCESS_WAIT_TIME = 120;

	public static void waitTillSucceded(TestProcessComponentListener listener, int maxSeconds) {
		H2HWaiter waiter = new H2HWaiter(maxSeconds);
		do {
			if (listener.hasExecutionFailed())
				Assert.fail("Execution of process failed");
			waiter.tickASecond();
		} while (!listener.hasExecutionSucceeded());
	}

	public static void executeProcessTillSucceded(IProcessComponent<?> process) {
		TestProcessComponentListener listener = new TestProcessComponentListener();
		process.attachListener(listener);
		try {
			process.execute();
			waitTillSucceded(listener, MAX_PROCESS_WAIT_TIME);
		} catch (InvalidProcessStateException | ProcessExecutionException ex) {
			System.err.println("ERROR: Cannot wait until process is done.");
			Assert.fail("Process failed with exception " + ex.getMessage());
		}
	}

	public static void executeProcessTillFailed(IProcessComponent<?> process) {
		try {
			process.execute();
			System.err.println("ERROR: Cannot wait until process is done.");
			Assert.fail("Process should fail");
		} catch (InvalidProcessStateException | ProcessExecutionException e) {
			// expected behavior
		}
	}
}
