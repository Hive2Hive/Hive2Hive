package org.hive2hive.processes.framework.concretes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hive2hive.processes.framework.ProcessState;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.Process;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class SequentialProcess extends Process {

	List<ProcessComponent> components = new ArrayList<ProcessComponent>();

	private int executionIndex = 0;
	private int rollbackIndex = 0;

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		// execute all child components
		while (!components.isEmpty() && executionIndex < components.size()
				&& getState() == ProcessState.RUNNING) {
			rollbackIndex = executionIndex;
			ProcessComponent next = components.get(executionIndex);
			next.start();
			executionIndex++;
		}

		// wait for all child components
		waitForAll();
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

	// TODO this method could also be implemented as decorator if necessary
	private void waitForAll() {
		if (getState() == ProcessState.RUNNING) {
	
			// if all child sync, then already completed
			if (checkIfAllFinished())
				return;
	
			// wait for async components
			final CountDownLatch latch = new CountDownLatch(1);
			ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
			ScheduledFuture<?> handle = executor.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					if (checkIfAllFinished() || getState() != ProcessState.RUNNING) {
						latch.countDown();
					}
				}
			}, 500, 500, TimeUnit.MILLISECONDS);
			
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			handle.cancel(true);
		}
	}

	private boolean checkIfAllFinished() {
		for (ProcessComponent component : components) {
			if (component.getState() != ProcessState.SUCCEEDED && component.getState() != ProcessState.FAILED)
				return false;
		}
		return true;
	}

}
