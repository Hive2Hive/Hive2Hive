package org.hive2hive.core.test.process;

import net.tomp2p.futures.FutureGet;
import net.tomp2p.futures.FutureRemove;

import org.hive2hive.core.network.messages.direct.response.ResponseMessage;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessState;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcessTest extends H2HJUnitTest {

	public ProcessTest() throws Exception {
		super();
	}

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ProcessTest.class;
		beforeClass();
	}

	@Test
	public void testProcessState() {
		Process process = generateProcess();
		Assert.assertEquals(ProcessState.INITIALIZING, process.getState());

		process.start();
		Assert.assertEquals(ProcessState.RUNNING, process.getState());

		process.pause();
		Assert.assertEquals(ProcessState.PAUSED, process.getState());

		process.continueProcess();
		Assert.assertEquals(ProcessState.RUNNING, process.getState());

		process.finalize();
		Assert.assertEquals(ProcessState.FINISHED, process.getState());
	}

	private Process generateProcess() {
		ProcessStep firstStep = new ProcessStep() {
			@Override
			public void start() {
				// do nothing
			}

			@Override
			public void rollBack() {
				// do nothing
			}

			@Override
			protected void handleMessageReply(ResponseMessage asyncReturnMessage) {
				// do nothing
			}

			@Override
			protected void handleGetResult(FutureGet future) {
				// do nothing
			}

			@Override
			protected void handleRemovalResult(FutureRemove future) {
				// do nothing
			}
		};

		Process process = new Process(null) {
		};
		process.setFirstStep(firstStep);
		return process;
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
