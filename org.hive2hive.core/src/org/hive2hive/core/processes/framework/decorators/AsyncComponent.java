package org.hive2hive.core.processes.framework.decorators;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hive2hive.core.exceptions.InvalidProcessStateException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.abstracts.ProcessDecorator;

public class AsyncComponent extends ProcessDecorator implements Callable<Boolean> {
	
	// TODO this class could hold a static thread pool to limit and manage all
	// asynchronous processes

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(AsyncComponent.class);
	
	private final ExecutorService executor;
	private Future<Boolean> handle;

	public AsyncComponent(ProcessComponent decoratedComponent) {
		super(decoratedComponent);

		executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void start() throws InvalidProcessStateException {
		super.start();

		handle = executor.submit(this);
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

		logger.debug("Starting async component...");
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
		
		// only called from outside (this is the AsyncComponent, not the decorated component)
		// decorated component handles itself
		// TODO but who tells AsyncComponents parent that failed?

		logger.debug(Thread.currentThread().getName());
		
		// cancel async thread
		if (!handle.isDone()) {
			logger.debug("Cancelling async thread.");
			handle.cancel(true); // TODO does the thread really need to be killed? or just awaited?
		}
		try {
			executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(e);
		}
		
		// sync again
		decoratedComponent.cancel(reason);

		super.cancel(reason);
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
