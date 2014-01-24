package org.hive2hive.processes.framework.decorators;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.abstracts.ProcessDecorator;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class AsyncComponent extends ProcessDecorator implements Callable<ProcessComponent> {

	// TODO this class could hold a static thread pool to limit and manage all
	// asynchronous processes

	public AsyncComponent(ProcessComponent decoratedComponent) {
		super(decoratedComponent);
	}

	@Override
	public void join() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(this);
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
	protected void doRollback(RollbackReason reason) {
		// TODO Auto-generated method stub

	}

	@Override
	public ProcessComponent call() throws Exception {

		try {
			Thread.currentThread().checkAccess();
			Thread.currentThread().setName(
					String.format("async-process %s ", decoratedComponent.getClass().getSimpleName()));
		} catch (SecurityException e) {
		}
		;

		decoratedComponent.start();
		return decoratedComponent;
	}

}
