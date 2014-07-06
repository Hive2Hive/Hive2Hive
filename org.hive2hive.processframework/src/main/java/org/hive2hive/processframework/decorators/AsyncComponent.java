package org.hive2hive.processframework.decorators;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hive2hive.processframework.ProcessState;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.Process;
import org.hive2hive.processframework.abstracts.ProcessDecorator;
import org.hive2hive.processframework.concretes.SequentialProcess;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ProcessDecorator} that executes, and if necessary rollbacks, the wrapped {@link IProcessComponent}
 * in an asynchronous manner. </br>
 * <b>Note: </b></br>
 * An asynchronous component is executed in an own thread and therefore independent of all other components in
 * a process composite. </br>
 * If existing, the parent container component of an {@link AsyncComponent} is responsible to await the result
 * of the asynchronous component. Therefore, the usage of {@link SequentialProcess} is highly recommended.
 * </br>
 * In case of a failure within the asynchronous component, it rollbacks itself in its own thread and returns
 * the resulting {@link RollbackReason}. In case the {@link AsyncComponent} needs to be cancelled due to a
 * failure in another place in the whole composite, the wrapped component (if necessary) is rolled back in the
 * detecting thread.
 * 
 * @author Christian
 * 
 */
public class AsyncComponent extends ProcessDecorator implements Callable<RollbackReason> {

	private static final Logger logger = LoggerFactory.getLogger(AsyncComponent.class);

	private final ExecutorService asyncExecutor;
	private Future<RollbackReason> handle;

	private boolean componentSucceeded = false;
	private boolean componentFailed = false;
	private RollbackReason result = null;

	public AsyncComponent(IProcessComponent decoratedComponent) {
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
			Thread.currentThread().setName("async proc");
		} catch (SecurityException e) {
			logger.error("Async thread cannot be renamed.", e);
		}
		;

		// starts and rolls back itself if needed (component knows nothing about the composite of which the
		// AsyncComponent is part of)

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

				if (getParent() == null) {
					try {
						cancel(reason);
					} catch (InvalidProcessStateException e) {
						logger.error("Asynchronous component could not be cancelled.", e);
					}
				}
			}
		});

		// sync execution
		decoratedComponent.start();

		return result;
	}

	@Override
	protected void doPause() throws InvalidProcessStateException {
		// mind: async component might be in any state!
		decoratedComponent.pause();
	}

	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		// mind: async component might be in any state!
		decoratedComponent.resume();
	}

	@Override
	protected void doResumeRollback() throws InvalidProcessStateException {
		// mind: async component might be in any state!
		decoratedComponent.resume();
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// mind: async component might be in any state!

		try {
			decoratedComponent.cancel(reason);
		} catch (InvalidProcessStateException e) {
			if (e.getCurrentState() == ProcessState.FAILED) {
				// async component rolled itself back already
				return;
			} else {
				throw e;
			}
		}
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
