package org.hive2hive.processes.framework.decorators;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.abstracts.ProcessDecorator;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class AsyncComponent extends ProcessDecorator implements Callable<Boolean> {

	// TODO this class could hold a static thread pool to limit and manage all
	// asynchronous processes
	
	private final ExecutorService executor;

	public AsyncComponent(ProcessComponent decoratedComponent) {
		super(decoratedComponent);

		executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void start() throws InvalidProcessStateException {
		super.start();

		executor.submit(this);
	}

	@Override
	public Boolean call() throws Exception {

		try {
			Thread.currentThread().checkAccess();
			Thread.currentThread().setName(
					String.format("async-process %s ", decoratedComponent.getClass().getSimpleName()));
		} catch (SecurityException e) {
		}
		;

		decoratedComponent.start();
		return true;
	}

	@Override
	public void pause() throws InvalidProcessStateException {
		super.pause();
		
		decoratedComponent.pause();
	}

	@Override
	public void resume() throws InvalidProcessStateException {
		super.resume();

		decoratedComponent.resume();
	}

	@Override
	public void cancel(RollbackReason reason) throws InvalidProcessStateException {
		super.cancel(reason);

		decoratedComponent.cancel(reason);
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// ignore, handled in component
	}

	@Override
	protected void doPause() {
		// ignore, handled in component
	}

	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		// ignore, handled in component
	}

	@Override
	protected void doResumeRollback() {
		// ignore, handled in component
	}

	@Override
	protected void doRollback(RollbackReason reason) {
		// ignore, handled in component
	}

}
