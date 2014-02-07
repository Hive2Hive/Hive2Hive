package org.hive2hive.core.processes.framework.concretes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

public class SequentialProcess extends Process {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(SequentialProcess.class);

	List<ProcessComponent> components = new ArrayList<ProcessComponent>();
	List<Future<RollbackReason>> asyncHandles = new ArrayList<Future<RollbackReason>>();

	private int executionIndex = 0;
	private int rollbackIndex = 0;

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		// execute all child components
		while (!components.isEmpty() && executionIndex < components.size()
				&& getState() == ProcessState.RUNNING) {

			checkForAsyncFails();
			rollbackIndex = executionIndex;
			ProcessComponent next = components.get(executionIndex);

			if (next instanceof AsyncComponent) {
				asyncHandles.add(((AsyncComponent) next).getHandle());
			}

			next.start();
			executionIndex++;
		}

		// wait for all child components
		if (getState() == ProcessState.RUNNING) {
			awaitAsync();
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

		// // wait for all child components
		// if (getState() == ProcessState.ROLLBACKING) {
		// waitForAllAsync("Waiting for all async components to finish rollback.");
		// }
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

	private void checkForAsyncFails() throws ProcessExecutionException {

		logger.debug("Checking async components for fails.");

		for (Future<RollbackReason> handle : asyncHandles) {
			if (handle.isDone()) {
				getResult(handle);
			}
		}
	}

	private void awaitAsync() throws ProcessExecutionException {

		logger.debug(String.format("Awaiting async components for completion."));

		if (getState() != ProcessState.RUNNING)
			return;
		
		final CountDownLatch latch = new CountDownLatch(1);
		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		ScheduledFuture<?> handle = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				
				if (getState() != ProcessState.RUNNING)
					latch.countDown();
				
				
			}
		}, 1, 1, TimeUnit.SECONDS);
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		handle.cancel(true);
		
	}

	private void getResult(Future<RollbackReason> handle) throws ProcessExecutionException {
		RollbackReason result = null;
		try {
			result = handle.get();
		} catch (InterruptedException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (ExecutionException e) {
			throw new ProcessExecutionException("AsyncComponent threw an exception.", e.getCause());
		}

		// initiate rollback if necessary
		if (result != null) {
			throw new ProcessExecutionException(result);
		}
	}

	// private boolean allFinished() {
	// for (ProcessComponent component : components) {
	// if (component.getState() == ProcessState.RUNNING)
	// return false;
	// if (component.getState() == ProcessState.ROLLBACKING)
	// return false;
	// }
	// return true;
	// }

}
