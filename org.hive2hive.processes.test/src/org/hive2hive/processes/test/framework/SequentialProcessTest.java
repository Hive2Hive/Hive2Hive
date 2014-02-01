package org.hive2hive.processes.test.framework;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.processes.framework.ProcessState;
import org.hive2hive.processes.framework.concretes.SequentialProcess;
import org.hive2hive.processes.framework.decorators.AsyncComponent;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.test.util.BusyFailingStep;
import org.hive2hive.processes.test.util.BusySucceedingStep;
import org.hive2hive.processes.test.util.FailingProcessStep;
import org.hive2hive.processes.test.util.FailingSequentialProcess;
import org.hive2hive.processes.test.util.SucceedingProcessStep;
import org.hive2hive.processes.test.util.TestProcessComponentListener;
import org.hive2hive.processes.test.util.TestUtil;
import org.hive2hive.processes.test.util.UseCaseTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SequentialProcessTest extends H2HJUnitTest {

	private final int MAX_ASYNC_WAIT = 5;

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = SequentialProcessTest.class;
		beforeClass();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}

	@Test
	public void syncSuccessTest() throws InvalidProcessStateException {

		// empty
		SequentialProcess process = new SequentialProcess();
		process.start();
		assertTrue(process.getState() == ProcessState.SUCCEEDED);

		// sync components
		process = new SequentialProcess();
		process.add(new SucceedingProcessStep());
		process.start();
		assertTrue(process.getState() == ProcessState.SUCCEEDED);

		// async components
		process = new SequentialProcess();
		process.add(new AsyncComponent(new BusySucceedingStep()));
		process.start();
		assertTrue(process.getState() == ProcessState.SUCCEEDED);

	}

	@Test
	public void syncFailTest() throws InvalidProcessStateException {
	
		// empty
		SequentialProcess process = new FailingSequentialProcess();
		process.start();
		assertTrue(process.getState() == ProcessState.FAILED);
	
		// sync components
		process = new SequentialProcess();
		process.add(new FailingProcessStep());
		process.start();
		assertTrue(process.getState() == ProcessState.FAILED);
	
		// async components
		process = new SequentialProcess();
		process.add(new AsyncComponent(new BusyFailingStep()));
		process.start();
		assertTrue(process.getState() == ProcessState.FAILED);
	}

	@Test
	public void asyncSuccessTest() throws InvalidProcessStateException {

		// empty
		SequentialProcess process = new SequentialProcess();
		AsyncComponent asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestUtil.wait(500);
		assertTrue(asyncProcess.getState() == ProcessState.SUCCEEDED);

		// sync components
		process = new SequentialProcess();
		process.add(new SucceedingProcessStep());
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestUtil.wait(500);
		assertTrue(asyncProcess.getState() == ProcessState.SUCCEEDED);

		// async components
		process = new SequentialProcess();
		process.add(new AsyncComponent(new BusySucceedingStep()));
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();
		assertFalse(asyncProcess.getState() == ProcessState.SUCCEEDED);

		TestUtil.wait(3500);
		assertTrue(asyncProcess.getState() == ProcessState.SUCCEEDED);
	}

	@Test
	public void syncListenerSuccessTest() throws InvalidProcessStateException {

		TestProcessComponentListener listener = new TestProcessComponentListener();

		// test success
		SequentialProcess successProcess = new SequentialProcess();
		successProcess.attachListener(listener);
		successProcess.start();

		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test success (1-layer, steps only)
		SequentialProcess successProcess2 = new SequentialProcess();
		successProcess2.add(new SucceedingProcessStep());
		successProcess2.add(new SucceedingProcessStep());
		successProcess2.attachListener(listener);
		successProcess2.start();

		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test success (1-layer, step and sub-process without children)
		SequentialProcess successProcess3 = new SequentialProcess();
		successProcess3.add(new SucceedingProcessStep());
		successProcess3.add(new SequentialProcess());
		successProcess3.attachListener(listener);
		successProcess3.start();

		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test success (2-layer, steps and sub-process with children)
		SequentialProcess successProcess4 = new SequentialProcess();
		SequentialProcess subProcess = new SequentialProcess();
		subProcess.add(new SucceedingProcessStep());
		subProcess.add(new SucceedingProcessStep());

		successProcess4.add(new SucceedingProcessStep());
		successProcess4.add(subProcess);
		successProcess4.add(new SucceedingProcessStep());
		successProcess4.attachListener(listener);
		successProcess4.start();

		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());

	}

	@Test
	public void syncListenerFailTest() throws InvalidProcessStateException {

		TestProcessComponentListener listener = new TestProcessComponentListener();

		// test fail
		SequentialProcess failProcess = new FailingSequentialProcess();
		failProcess.attachListener(listener);
		failProcess.start();

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (1-layer, steps only)
		SequentialProcess failProcess2 = new SequentialProcess();
		failProcess2.add(new SucceedingProcessStep());
		failProcess2.add(new FailingProcessStep());
		failProcess2.attachListener(listener);
		failProcess2.start();

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (1-layer, step and sub-process (failing) without children)
		SequentialProcess failProcess3 = new SequentialProcess();
		failProcess3.add(new SucceedingProcessStep());
		failProcess3.add(new FailingSequentialProcess());
		failProcess3.attachListener(listener);
		failProcess3.start();

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (2-layer, steps (failing) and sub-process with children)
		SequentialProcess failProcess4 = new SequentialProcess();
		SequentialProcess successSubProcess = new SequentialProcess();
		successSubProcess.add(new SucceedingProcessStep());
		successSubProcess.add(new SucceedingProcessStep());

		failProcess4.add(new SucceedingProcessStep());
		failProcess4.add(successSubProcess);
		failProcess4.add(new FailingProcessStep());
		failProcess4.attachListener(listener);
		failProcess4.start();

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (2-layer, steps and sub-process (failing) with children)
		SequentialProcess failProcess5 = new SequentialProcess();
		SequentialProcess failSubProcess = new FailingSequentialProcess();

		failProcess5.add(new SucceedingProcessStep());
		failProcess5.add(failSubProcess);
		failProcess5.add(new SucceedingProcessStep());
		failProcess5.attachListener(listener);
		failProcess5.start();

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (2-layer, steps and sub-process with children (failing))
		SequentialProcess failProcess6 = new SequentialProcess();
		SequentialProcess failSubProcess2 = new SequentialProcess();
		failSubProcess2.add(new SucceedingProcessStep());
		failSubProcess2.add(new FailingProcessStep());

		failProcess6.add(new SucceedingProcessStep());
		failProcess6.add(failSubProcess2);
		failProcess6.add(new SucceedingProcessStep());
		failProcess6.attachListener(listener);
		failProcess6.start();

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());
	}

	@Test
	public void asyncListenerSuccessTest() throws InvalidProcessStateException {

		TestProcessComponentListener listener = new TestProcessComponentListener();

		// test success
		AsyncComponent successProcess = new AsyncComponent(new SequentialProcess());
		successProcess.attachListener(listener);
		successProcess.start();

		UseCaseTestUtil.waitTillSucceded(listener, MAX_ASYNC_WAIT);

		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test success (1-layer, steps only)
		SequentialProcess successProcess2 = new SequentialProcess();
		successProcess2.add(new SucceedingProcessStep());
		successProcess2.add(new SucceedingProcessStep());

		AsyncComponent asyncProcess2 = new AsyncComponent(successProcess2);
		asyncProcess2.attachListener(listener);
		asyncProcess2.start();

		UseCaseTestUtil.waitTillSucceded(listener, MAX_ASYNC_WAIT);

		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test success (1-layer, step and sub-process without children)
		SequentialProcess successProcess3 = new SequentialProcess();
		successProcess3.add(new SucceedingProcessStep());
		successProcess3.add(new SequentialProcess());

		AsyncComponent asyncProcess3 = new AsyncComponent(successProcess3);
		asyncProcess3.attachListener(listener);
		asyncProcess3.start();

		UseCaseTestUtil.waitTillSucceded(listener, MAX_ASYNC_WAIT);

		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test success (2-layer, steps and sub-process with children)
		SequentialProcess successProcess4 = new SequentialProcess();
		SequentialProcess subProcess = new SequentialProcess();
		subProcess.add(new SucceedingProcessStep());
		subProcess.add(new SucceedingProcessStep());

		successProcess4.add(new SucceedingProcessStep());
		successProcess4.add(subProcess);
		successProcess4.add(new SucceedingProcessStep());

		AsyncComponent asyncProcess4 = new AsyncComponent(successProcess4);
		asyncProcess4.attachListener(listener);
		asyncProcess4.start();

		UseCaseTestUtil.waitTillSucceded(listener, MAX_ASYNC_WAIT);

		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());
	}

	@Test
	public void asyncListenerFailTest() throws InvalidProcessStateException {

		TestProcessComponentListener listener = new TestProcessComponentListener();

		// test fail
		AsyncComponent failProcess = new AsyncComponent(new FailingSequentialProcess());
		failProcess.attachListener(listener);
		failProcess.start();

		UseCaseTestUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (1-layer, steps only)
		SequentialProcess failProcess2 = new SequentialProcess();
		failProcess2.add(new SucceedingProcessStep());
		failProcess2.add(new FailingProcessStep());

		AsyncComponent asyncProcess2 = new AsyncComponent(failProcess2);
		asyncProcess2.attachListener(listener);
		asyncProcess2.start();

		UseCaseTestUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (1-layer, step and sub-process (failing) without children)
		SequentialProcess failProcess3 = new SequentialProcess();
		failProcess3.add(new SucceedingProcessStep());
		failProcess3.add(new FailingSequentialProcess());

		AsyncComponent asyncProcess3 = new AsyncComponent(failProcess3);
		asyncProcess3.attachListener(listener);
		asyncProcess3.start();

		UseCaseTestUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (2-layer, steps (failing) and sub-process with children)
		SequentialProcess failProcess4 = new SequentialProcess();
		SequentialProcess successSubProcess = new SequentialProcess();
		successSubProcess.add(new SucceedingProcessStep());
		successSubProcess.add(new SucceedingProcessStep());

		failProcess4.add(new SucceedingProcessStep());
		failProcess4.add(successSubProcess);
		failProcess4.add(new FailingProcessStep());

		AsyncComponent asyncProcess4 = new AsyncComponent(failProcess4);
		asyncProcess4.attachListener(listener);
		asyncProcess4.start();

		UseCaseTestUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (2-layer, steps and sub-process (failing) with children)
		SequentialProcess failProcess5 = new SequentialProcess();
		SequentialProcess failSubProcess = new FailingSequentialProcess();

		failProcess5.add(new SucceedingProcessStep());
		failProcess5.add(failSubProcess);
		failProcess5.add(new SucceedingProcessStep());

		AsyncComponent asyncProcess5 = new AsyncComponent(failProcess5);
		asyncProcess5.attachListener(listener);
		asyncProcess5.start();

		UseCaseTestUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());

		listener.reset();

		// test fail (2-layer, steps and sub-process with children (failing))
		SequentialProcess failProcess6 = new SequentialProcess();
		SequentialProcess failSubProcess2 = new SequentialProcess();
		failSubProcess2.add(new SucceedingProcessStep());
		failSubProcess2.add(new FailingProcessStep());

		failProcess6.add(new SucceedingProcessStep());
		failProcess6.add(failSubProcess2);
		failProcess6.add(new SucceedingProcessStep());

		AsyncComponent asyncProcess6 = new AsyncComponent(failProcess6);
		asyncProcess6.attachListener(listener);
		asyncProcess6.start();

		UseCaseTestUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());
	}
}
