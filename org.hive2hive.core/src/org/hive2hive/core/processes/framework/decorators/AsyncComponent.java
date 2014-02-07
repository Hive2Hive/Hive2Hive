package org.hive2hive.core.processes.framework.decorators;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.ProcessState;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.abstracts.Process;
import org.hive2hive.core.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.core.processes.framework.abstracts.ProcessDecorator;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponentListener;

public class AsyncComponent extends ProcessDecorator implements Callable<RollbackReason> {

	// TODO this class could hold a static thread pool to limit and manage all
	// asynchronous processes

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(AsyncComponent.class);

	private final ExecutorService asyncExecutor;
	private Future<RollbackReason> handle;

	private boolean componentSucceeded = false;
	private boolean componentFailed = false;
	private RollbackReason result = null;

	public AsyncComponent(ProcessComponent decoratedComponent) {
		super(decoratedComponent);
		
		asyncExecutor = Executors.newSingleThreadExecutor();
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		handle = asyncExecutor.submit(this);
		// immediate return, since execution is async

	}

	@Override
	public RollbackReason call() throws Exception {

		try {
			Thread.currentThread().checkAccess();
			Thread.currentThread().setName(
					String.format("async-process %s ", decoratedComponent.getClass().getSimpleName()));
		} catch (SecurityException e) {
		}
		;

		logger.debug("Starting async component...");

		// starts and rollbacks itself if needed (component knows nothing about composite the AsyncComponent
		// is part of)

		decoratedComponent.attachListener(new IProcessComponentListener() {

			@Override
			public void onSucceeded() {
				componentSucceeded = true;
				componentFailed = false;
				result = null;
				succeed();
			}

			@Override
			public void onFailed(RollbackReason reason) {
				componentSucceeded = false;
				componentFailed = true;
				result = reason;
				fail(reason);
			}

			@Override
			public void onFinished() {
				// ignore
			}
		});

		// sync execution
		decoratedComponent.start();

		return result;
	}

	@Override
	protected void doPause() {
		// attention: component might be in any state!!!
	}

	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		// attention: component might be in any state!!!
	}

	@Override
	protected void doResumeRollback() {
		// attention: component might be in any state!!!
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {

		// called due to fail in other component (sibling of AsyncComponent)

		decoratedComponent.cancel(reason); // attention: component might be in any state!!! (when RB comes
											// from sibling component)
	}

	@Override
	protected void succeed() {
		// AsyncComponent does not succeed until component does
		if (componentSucceeded) {
			super.succeed();
		}
	}

	@Override
	protected void fail(RollbackReason reason) {
		// AsyncComponent does not fail until component does
		if (componentFailed) {
			super.fail(reason);
		}
	}

	@Override
	public void attachListener(IProcessComponentListener listener) {
		decoratedComponent.attachListener(listener);
	}

	@Override
	public void detachListener(IProcessComponentListener listener) {
		decoratedComponent.attachListener(listener);
	}

	@Override
	public List<IProcessComponentListener> getListener() {
		return decoratedComponent.getListener();
	}

	@Override
	public String getID() {
		return decoratedComponent.getID();
	}

	@Override
	public double getProgress() {
		return decoratedComponent.getProgress();
	}

	@Override
	public ProcessState getState() {
		// return state of AsyncComponent, not of decorated component
		return super.getState();
	}

	@Override
	public void setParent(Process parent) {
		super.setParent(parent);
	}

	@Override
	public Process getParent() {
		return super.getParent();
	}

	public Future<RollbackReason> getHandle() {
		return handle;
	}

}
