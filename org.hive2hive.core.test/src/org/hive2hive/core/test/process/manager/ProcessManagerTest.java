package org.hive2hive.core.test.process.manager;

import java.util.Random;

import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.ProcessManager;
import org.hive2hive.core.process.ProcessState;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcessManagerTest extends H2HJUnitTest {

	public ProcessManagerTest() throws Exception {
		super();
	}

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ProcessManagerTest.class;
		beforeClass();
	}

	@Test
	public void testIncreasingPID() {
		ProcessManager manager = ProcessManager.getInstance();
		int first = manager.getNewPID();
		int second = manager.getNewPID();
		Assert.assertTrue(first != second);
	}

	@Test
	public void testAttachDetach() {
		IProcess process = generateProcess();
		ProcessManager manager = ProcessManager.getInstance();

		// attach and fetch
		manager.attachProcess(process);
		IProcess sameProcess = manager.getProcess(process.getID());
		Assert.assertEquals(process.getID(), sameProcess.getID());

		// detach and fetch
		manager.detachProcess(process);
		IProcess none = manager.getProcess(process.getID());
		Assert.assertNull(none);
	}

	@Test
	public void testAttachTwice() {
		IProcess process = generateProcess();
		ProcessManager manager = ProcessManager.getInstance();

		// attach twice
		manager.attachProcess(process);
		try {
			manager.attachProcess(process);
			Assert.fail("Should have thrown exception when adding process twice");
		} catch (IllegalArgumentException e) {
			// success
		}
	}

	private IProcess generateProcess() {
		return new IProcess() {
			private int id = new Random().nextInt(100);

			@Override
			public void stop(String reason) {
			}

			@Override
			public void start() {
			}

			@Override
			public void pause() {
			}

			@Override
			public ProcessState getState() {
				return ProcessState.RUNNING;
			}

			@Override
			public int getProgress() {
				return new Random().nextInt(100);
			}

			@Override
			public int getID() {
				return id;
			}

			@Override
			public void continueProcess() {
			}

			@Override
			public void run() {

			}

			@Override
			public void addListener(IProcessListener listener) {

			}

			@Override
			public boolean removeListener(IProcessListener listener) {
				return false;
			}

			@Override
			public void terminate() {
			}
		};
	}

	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}
}
