package org.hive2hive.processes.framework.decorators;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.abstracts.ProcessDecorator;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;

public class AsyncComponent extends ProcessDecorator implements Callable<Boolean> {

	private final static H2HLogger logger = H2HLoggerFactory.getLogger(AsyncComponent.class);

	// TODO this class could hold a static thread pool to limit and manage all
	// asynchronous processes
	private final ExecutorService executor;

	private boolean componentSucceeded = false;
	private boolean componentFailed = false;

	public AsyncComponent(ProcessComponent decoratedComponent) {
		super(decoratedComponent);

		executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void join() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		IProcessComponentListener componentListener = new IProcessComponentListener() {

			@Override
			public void onSucceeded() {
				componentSucceeded = true;
				succeed();
			}

			@Override
			public void onFailed() {
				componentFailed = true;
				fail();
			}

			@Override
			public void onFinished() {
				// ignore
			}
		};
		decoratedComponent.attachListener(componentListener);

		executor.submit(this);
	}

	@Override
	protected void succeed() {

		// AsyncComponent succeeds when its async decorated component succeeds
		if (componentSucceeded) {
			super.succeed();
		}
	}
	
	@Override
	protected void fail() {

		if (componentFailed) {
			super.fail();
		}
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

}
