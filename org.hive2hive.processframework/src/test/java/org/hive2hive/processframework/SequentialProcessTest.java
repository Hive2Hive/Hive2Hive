package org.hive2hive.processframework;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.concretes.SequentialProcess;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.util.BusyFailingStep;
import org.hive2hive.processframework.util.BusySucceedingStep;
import org.hive2hive.processframework.util.FailingProcessStep;
import org.hive2hive.processframework.util.FailingSequentialProcess;
import org.hive2hive.processframework.util.SucceedingProcessStep;
import org.hive2hive.processframework.util.TestExecutionUtil;
import org.hive2hive.processframework.util.TestProcessComponentListener;
import org.junit.Test;

public class SequentialProcessTest {

	private final int MAX_ASYNC_WAIT = 5;
	private final int WAIT_FOR_ASYNC = TestExecutionUtil.DEFAULT_WAITING_TIME + 1500;

	@Test
	public void syncSuccessTest() throws InvalidProcessStateException {

		// empty
		SequentialProcess process = new SequentialProcess();
		process.start();
		assertTrue(process.getState() == ProcessState.SUCCEEDED);

		// sync components
		process = new SequentialProcess();
		ProcessStep step = new SucceedingProcessStep();
		process.add(step);
		process.start();
		assertTrue(process.getState() == ProcessState.SUCCEEDED);
		assertTrue(step.getState() == ProcessState.SUCCEEDED);

		// async components
		process = new SequentialProcess();
		ProcessComponent asyncStep = new AsyncComponent(new BusySucceedingStep());
		process.add(asyncStep);
		process.start();
		assertTrue(process.getState() == ProcessState.SUCCEEDED);
		assertTrue(asyncStep.getState() == ProcessState.SUCCEEDED);
	}

	@Test
	public void syncFailTest() throws InvalidProcessStateException {

		// empty
		SequentialProcess process = new FailingSequentialProcess();
		process.start();
		assertTrue(process.getState() == ProcessState.FAILED);

		// sync components
		process = new SequentialProcess();
		ProcessStep step = new FailingProcessStep();
		process.add(step);
		process.start();
		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step.getState() == ProcessState.FAILED);

		// async components
		process = new SequentialProcess();
		ProcessComponent asyncStep = new AsyncComponent(new BusyFailingStep());
		process.add(asyncStep);
		process.start();
		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(asyncStep.getState() == ProcessState.FAILED);
	}

	@Test
	public void asyncSuccessTest() throws InvalidProcessStateException {

		// empty
		SequentialProcess process = new SequentialProcess();
		AsyncComponent asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);
		assertTrue(asyncProcess.getState() == ProcessState.SUCCEEDED);

		// sync components
		process = new SequentialProcess();
		process.add(new BusySucceedingStep());
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();
		assertFalse(asyncProcess.getState() == ProcessState.SUCCEEDED);

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);
		assertTrue(asyncProcess.getState() == ProcessState.SUCCEEDED);

		// async components
		process = new SequentialProcess();
		process.add(new AsyncComponent(new BusySucceedingStep()));
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();
		assertFalse(asyncProcess.getState() == ProcessState.SUCCEEDED);

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);
		assertTrue(asyncProcess.getState() == ProcessState.SUCCEEDED);
	}

	@Test
	public void asyncFailTest() throws InvalidProcessStateException {

		// empty
		SequentialProcess process = new FailingSequentialProcess();
		AsyncComponent asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);
		assertTrue(asyncProcess.getState() == ProcessState.FAILED);
		assertTrue(process.getState() == ProcessState.FAILED);

		// sync components
		process = new SequentialProcess();
		process.add(new BusyFailingStep());
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);
		assertTrue(asyncProcess.getState() == ProcessState.FAILED);
		assertTrue(process.getState() == ProcessState.FAILED);

		// async components
		process = new SequentialProcess();
		process.add(new AsyncComponent(new BusyFailingStep()));
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);
		assertTrue(asyncProcess.getState() == ProcessState.FAILED);
		assertTrue(process.getState() == ProcessState.FAILED);
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

		TestExecutionUtil.waitTillSucceded(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillSucceded(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillSucceded(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillSucceded(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

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

		TestExecutionUtil.waitTillFailed(listener, MAX_ASYNC_WAIT);

		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());
	}

	@Test
	public void syncRollbackTest() throws InvalidProcessStateException {

		// sync components (level-1 fail)
		SequentialProcess subProcess = new SequentialProcess();
		ProcessStep subStep1 = new SucceedingProcessStep();
		ProcessStep subStep2 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);

		SequentialProcess process = new SequentialProcess();
		ProcessStep step1 = new SucceedingProcessStep();
		ProcessStep step2 = new SucceedingProcessStep();
		ProcessStep step3 = new FailingProcessStep();
		ProcessStep step4 = new SucceedingProcessStep();
		process.add(step1);
		process.add(step2);
		process.add(subProcess);
		process.add(step3);
		process.add(step4);
		process.start();

		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(step3.getState() == ProcessState.FAILED);
		assertTrue(step4.getState() == ProcessState.READY);

		// sync components (level-2 fail)
		subProcess = new SequentialProcess();
		subStep1 = new SucceedingProcessStep();
		subStep2 = new FailingProcessStep();
		ProcessStep subStep3 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);
		subProcess.add(subStep3);

		process = new SequentialProcess();
		step1 = new SucceedingProcessStep();
		step2 = new SucceedingProcessStep();
		step3 = new SucceedingProcessStep();
		process.add(step1);
		process.add(step2);
		process.add(subProcess);
		process.add(step3);
		process.start();

		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(subStep3.getState() == ProcessState.READY);
		assertTrue(step3.getState() == ProcessState.READY);

		// async components (level-1 fail)
		// sync step fails
		subProcess = new SequentialProcess();
		subStep1 = new SucceedingProcessStep();
		subStep2 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);

		process = new SequentialProcess();
		step1 = new SucceedingProcessStep();
		step2 = new BusySucceedingStep(); // make sure rollback waits for all async components
		AsyncComponent asyncStep2 = new AsyncComponent(step2);
		step3 = new FailingProcessStep();
		step4 = new SucceedingProcessStep();
		process.add(step1);
		process.add(asyncStep2);
		process.add(subProcess);
		process.add(step3);
		process.add(step4);
		process.start();

		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(asyncStep2.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(step3.getState() == ProcessState.FAILED);
		assertTrue(step4.getState() == ProcessState.READY);

		// async step fails
		subProcess = new SequentialProcess();
		subStep1 = new SucceedingProcessStep();
		subStep2 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);

		process = new SequentialProcess();
		step1 = new SucceedingProcessStep();
		step2 = new SucceedingProcessStep();
		step3 = new BusyFailingStep(); // make sure rollback waits for all async components
		AsyncComponent asyncStep3 = new AsyncComponent(step3);
		step4 = new SucceedingProcessStep();
		process.add(step1);
		process.add(step2);
		process.add(subProcess);
		process.add(asyncStep3);
		process.add(step4);
		process.start();

		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(asyncStep3.getState() == ProcessState.FAILED);
		assertTrue(step3.getState() == ProcessState.FAILED);
		assertTrue(step4.getState() == ProcessState.READY || step4.getState() == ProcessState.FAILED);

		// async components (level-2 fail)
		subProcess = new SequentialProcess();
		subStep1 = new SucceedingProcessStep();
		subStep2 = new FailingProcessStep();
		subStep3 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);
		subProcess.add(subStep3);
		AsyncComponent asyncSubProcess = new AsyncComponent(subProcess);

		process = new SequentialProcess();
		step1 = new SucceedingProcessStep();
		step2 = new SucceedingProcessStep();
		step3 = new SucceedingProcessStep();
		process.add(step1);
		process.add(step2);
		process.add(asyncSubProcess);
		process.add(step3);
		process.start();

		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(asyncSubProcess.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(subStep3.getState() == ProcessState.READY);
		assertTrue(step3.getState() == ProcessState.READY || step3.getState() == ProcessState.FAILED);
	}

	@Test
	public void asyncRollbackTest() throws InvalidProcessStateException {

		// sync components (level-1 fail)
		SequentialProcess subProcess = new SequentialProcess();
		ProcessStep subStep1 = new SucceedingProcessStep();
		ProcessStep subStep2 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);

		SequentialProcess process = new SequentialProcess();
		ProcessStep step1 = new SucceedingProcessStep();
		ProcessStep step2 = new SucceedingProcessStep();
		ProcessStep step3 = new FailingProcessStep();
		ProcessStep step4 = new SucceedingProcessStep();
		process.add(step1);
		process.add(step2);
		process.add(subProcess);
		process.add(step3);
		process.add(step4);
		AsyncComponent asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);

		assertTrue(asyncProcess.getState() == ProcessState.FAILED);
		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(step3.getState() == ProcessState.FAILED);
		assertTrue(step4.getState() == ProcessState.READY);

		// sync components (level-2 fail)
		subProcess = new SequentialProcess();
		subStep1 = new SucceedingProcessStep();
		subStep2 = new FailingProcessStep();
		ProcessStep subStep3 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);
		subProcess.add(subStep3);

		process = new SequentialProcess();
		step1 = new SucceedingProcessStep();
		step2 = new SucceedingProcessStep();
		step3 = new SucceedingProcessStep();
		process.add(step1);
		process.add(step2);
		process.add(subProcess);
		process.add(step3);
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);

		assertTrue(asyncProcess.getState() == ProcessState.FAILED);
		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(subStep3.getState() == ProcessState.READY);
		assertTrue(step3.getState() == ProcessState.READY);

		// async components (level-1 fail)
		// sync step fails
		subProcess = new SequentialProcess();
		subStep1 = new SucceedingProcessStep();
		subStep2 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);

		process = new SequentialProcess();
		step1 = new SucceedingProcessStep();
		step2 = new BusySucceedingStep(); // make sure rollback waits for all async components
		AsyncComponent asyncStep2 = new AsyncComponent(step2);
		step3 = new FailingProcessStep();
		step4 = new SucceedingProcessStep();
		process.add(step1);
		process.add(asyncStep2);
		process.add(subProcess);
		process.add(step3);
		process.add(step4);
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);

		assertTrue(asyncProcess.getState() == ProcessState.FAILED);
		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(asyncStep2.getState() == ProcessState.FAILED);
		assertTrue(asyncStep2.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(step3.getState() == ProcessState.FAILED);
		assertTrue(step4.getState() == ProcessState.READY);

		// async step fails
		subProcess = new SequentialProcess();
		subStep1 = new SucceedingProcessStep();
		subStep2 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);

		process = new SequentialProcess();
		step1 = new SucceedingProcessStep();
		step2 = new SucceedingProcessStep();
		step3 = new BusyFailingStep(); // make sure rollback waits for all async components
		AsyncComponent asyncStep3 = new AsyncComponent(step3);
		step4 = new SucceedingProcessStep();
		process.add(step1);
		process.add(step2);
		process.add(subProcess);
		process.add(asyncStep3);
		process.add(step4);
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);

		assertTrue(asyncProcess.getState() == ProcessState.FAILED);
		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(asyncStep3.getState() == ProcessState.FAILED);
		assertTrue(step3.getState() == ProcessState.FAILED);
		assertTrue(step4.getState() == ProcessState.READY || step4.getState() == ProcessState.FAILED);

		// async components (level-2 fail)
		subProcess = new SequentialProcess();
		subStep1 = new SucceedingProcessStep();
		subStep2 = new FailingProcessStep();
		subStep3 = new SucceedingProcessStep();
		subProcess.add(subStep1);
		subProcess.add(subStep2);
		subProcess.add(subStep3);
		AsyncComponent asyncSubProcess = new AsyncComponent(subProcess);

		process = new SequentialProcess();
		step1 = new SucceedingProcessStep();
		step2 = new SucceedingProcessStep();
		step3 = new SucceedingProcessStep();
		process.add(step1);
		process.add(step2);
		process.add(asyncSubProcess);
		process.add(step3);
		asyncProcess = new AsyncComponent(process);
		asyncProcess.start();

		TestExecutionUtil.wait(WAIT_FOR_ASYNC);

		assertTrue(asyncProcess.getState() == ProcessState.FAILED);
		assertTrue(process.getState() == ProcessState.FAILED);
		assertTrue(step1.getState() == ProcessState.FAILED);
		assertTrue(step2.getState() == ProcessState.FAILED);
		assertTrue(asyncSubProcess.getState() == ProcessState.FAILED);
		assertTrue(subProcess.getState() == ProcessState.FAILED);
		assertTrue(subStep1.getState() == ProcessState.FAILED);
		assertTrue(subStep2.getState() == ProcessState.FAILED);
		assertTrue(subStep3.getState() == ProcessState.READY);
		assertTrue(step3.getState() == ProcessState.READY || step3.getState() == ProcessState.FAILED);
	}

	@Test
	public void awaitSyncTest() throws InvalidProcessStateException, InterruptedException {

		// succeeding process
		SequentialProcess process = new SequentialProcess();
		process.add(new BusySucceedingStep());
		process.start();
		process.await();
		if (process.getState() != ProcessState.SUCCEEDED)
			fail("Busy process should have finished. Await() did not block.");
		TestExecutionUtil.wait(TestExecutionUtil.DEFAULT_WAITING_TIME);
		assertTrue(process.getState() == ProcessState.SUCCEEDED);

		// failing process
		SequentialProcess process2 = new SequentialProcess();
		process2.add(new BusyFailingStep());
		process2.start();
		process2.await();
		if (process2.getState() != ProcessState.FAILED)
			fail("Busy process should have finished. Await() did not block.");
		TestExecutionUtil.wait(TestExecutionUtil.DEFAULT_WAITING_TIME);
		assertTrue(process2.getState() == ProcessState.FAILED);
	}

	@Test
	public void awaitAsyncTest() throws InvalidProcessStateException, InterruptedException {

		// succeeding process
		SequentialProcess process = new SequentialProcess();
		process.add(new BusySucceedingStep());
		AsyncComponent asyncProcess = new AsyncComponent(process);
		asyncProcess.start();
		asyncProcess.await();
		if (asyncProcess.getState() != ProcessState.SUCCEEDED)
			fail("Busy process should have finished. Await() did not block.");
		TestExecutionUtil.wait(TestExecutionUtil.DEFAULT_WAITING_TIME);
		assertTrue(asyncProcess.getState() == ProcessState.SUCCEEDED);

		// failing process
		SequentialProcess process2 = new SequentialProcess();
		process2.add(new BusyFailingStep());
		AsyncComponent asyncProcess2 = new AsyncComponent(process2);
		asyncProcess2.start();
		asyncProcess2.await();
		if (asyncProcess2.getState() != ProcessState.FAILED)
			fail("Busy process should have finished. Await() did not block.");
		TestExecutionUtil.wait(TestExecutionUtil.DEFAULT_WAITING_TIME);
		assertTrue(asyncProcess2.getState() == ProcessState.FAILED);
	}
}