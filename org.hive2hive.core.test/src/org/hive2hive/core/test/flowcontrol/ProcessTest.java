package org.hive2hive.core.test.flowcontrol;

import org.hive2hive.core.flowcontrol.Process;
import org.hive2hive.core.flowcontrol.ProcessState;
import org.hive2hive.core.flowcontrol.ProcessStep;
import org.hive2hive.core.test.H2HJUnitTest;
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
		};

		return new Process(null, firstStep) {
		};
	}
}
