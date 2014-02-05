package org.hive2hive.core.processes.framework.concretes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.ProcessState;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.Process;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class SequentialProcess extends Process {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(SequentialProcess.class);
	
	List<ProcessComponent> components = new ArrayList<ProcessComponent>();

	private int executionIndex = 0;
	private int rollbackIndex = 0;

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		// execute all child components
		while (!components.isEmpty() && executionIndex < components.size()
				&& getState() == ProcessState.RUNNING) {
			checkExecutedComponents();
			rollbackIndex = executionIndex;
			ProcessComponent next = components.get(executionIndex);
			next.start();
			executionIndex++;
		}

		// wait for all child components
		if (getState() == ProcessState.RUNNING) {
			waitForAllAsync("Waiting for all async components to finish execution.");
		}
	}
	
	private void checkExecutedComponents() throws ProcessExecutionException {
		
		// an asychronous component might have failed meanwhile
		for (int i = 0; i < executionIndex; i++) {
			if (components.get(i).getState() == ProcessState.FAILED) {
				throw new ProcessExecutionException("An (async) executed component failed."); 
			}
		}
	}
	
	@Override
	protected void doPause() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doResumeRollback() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {

		while (!components.isEmpty() && rollbackIndex >= 0 && getState() == ProcessState.ROLLBACKING) {
			ProcessComponent last = components.get(rollbackIndex);
			last.cancel(reason);
			rollbackIndex--;
		}

		// wait for all child components
		if (getState() == ProcessState.ROLLBACKING) {
			waitForAllAsync("Waiting for all async components to finish rollback.");
		}
	}

	@Override
	protected void doAdd(ProcessComponent component) {
		components.add(component);
	}

	@Override
	protected void doInsert(int index, ProcessComponent component) {
		components.add(index, component);
	}

	@Override
	protected void doRemove(ProcessComponent component) {
		components.remove(component);
	}

	@Override
	public List<ProcessComponent> getComponents() {
		return Collections.unmodifiableList(components);
	}

	// TODO this method could also be implemented as decorator if necessary
	private void waitForAllAsync(final String message) {

		logger.debug("Starting " + message);
		
		// if all child sync, then already completed
		if (allFinished())
			return;

		// wait for async components
		final CountDownLatch latch = new CountDownLatch(1);
		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		ScheduledFuture<?> handle = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				logger.debug(message);
				if (allFinished() || getState() != ProcessState.RUNNING) { // TODO correct state check also for rollbacking
					latch.countDown();
				}
			}
		}, 1, 1, TimeUnit.SECONDS);

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handle.cancel(true);
	}

	private boolean allFinished() {
		for (ProcessComponent component : components) {
			if (component.getState() == ProcessState.RUNNING)
				return false;
			if (component.getState() == ProcessState.ROLLBACKING)
				return false;
		}
		return true;
	}

}
