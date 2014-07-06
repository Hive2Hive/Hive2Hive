package org.hive2hive.processframework.concretes;

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

import org.hive2hive.processframework.ProcessState;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.Process;
import org.hive2hive.processframework.abstracts.ProcessComponent;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A process component container that executes and rollbacks its process components in a sequential manner. In
 * presence
 * of asynchronous child components, this container waits for all asynchronous operations to complete before
 * continuing.
 * 
 * @author Christian
 */
public class SequentialProcess extends Process {

	private static final Logger logger = LoggerFactory.getLogger(SequentialProcess.class);

	private List<ProcessComponent> components = new ArrayList<ProcessComponent>();
	private List<Future<RollbackReason>> asyncHandles = new ArrayList<Future<RollbackReason>>();
	private ProcessExecutionException exception = null;

	private int executionIndex = 0;
	private int rollbackIndex = 0;

	@Override
	protected void doExecute() throws InvalidProcessStateException, ProcessExecutionException {

		// execute all child components
		while (!components.isEmpty() && executionIndex < components.size() && getState() == ProcessState.RUNNING) {

			checkAsyncComponentsForFail(asyncHandles);
			rollbackIndex = executionIndex;
			ProcessComponent next = components.get(executionIndex);
			next.start();
			executionIndex++;

			if (next instanceof AsyncComponent) {
				asyncHandles.add(((AsyncComponent) next).getHandle());
			}
		}

		// wait for async child components
		awaitAsync();
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

	private void awaitAsync() throws ProcessExecutionException {
		if (asyncHandles.isEmpty()) {
			return;
		}
		if (getState() != ProcessState.RUNNING) {
			return;
		}

		logger.trace("Awaiting async components for completion.");

		final CountDownLatch latch = new CountDownLatch(1);
		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		ScheduledFuture<?> handle = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {

				// assure still in running state
				if (getState() != ProcessState.RUNNING) {
					latch.countDown();
					return;
				}

				// check for potential fails
				try {
					checkAsyncComponentsForFail(asyncHandles);
				} catch (ProcessExecutionException e) {
					logger.error("Cannot await until the process finished", e);
					exception = e;
					latch.countDown();
					return;
				}

				// check for completion
				for (Future<RollbackReason> handle : asyncHandles) {
					if (!handle.isDone()) {
						return;
					}
				}
				latch.countDown();
			}
		}, 1, 1, TimeUnit.SECONDS);

		// blocking wait for completion or potential fail
		try {
			latch.await();
		} catch (InterruptedException e) {
			logger.error("Exception while waiting for async components.", e);
		}
		handle.cancel(true);

		if (exception != null) {
			throw exception;
		}
	}

	private static void checkAsyncComponentsForFail(List<Future<RollbackReason>> handles) throws ProcessExecutionException {

		if (handles.isEmpty()) {
			return;
		}

		for (Future<RollbackReason> handle : handles) {

			if (!handle.isDone()) {
				continue;
			}

			RollbackReason result = null;
			try {
				result = handle.get();
			} catch (InterruptedException e) {
				logger.error("Error while checking async component.", e);
			} catch (ExecutionException e) {
				logger.error("AsyncComponent threw an exception.");
				throw new ProcessExecutionException("AsyncComponent threw an exception.", e.getCause());
			}

			// initiate rollback if necessary
			if (result != null) {
				throw new ProcessExecutionException(result);
			}
		}
	}

}
