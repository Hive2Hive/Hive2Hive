package org.hive2hive.processframework.abstracts;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hive2hive.processframework.ProcessState;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all process components. Keeps track of a components most essential properties and
 * functionalities, such as state, progess and listeners.
 * 
 * @author Christian
 * 
 */
public abstract class ProcessComponent implements IProcessComponent {

	private static final Logger logger = LoggerFactory.getLogger(ProcessComponent.class);

	private final String id;
	private double progress;
	private ProcessState state;
	private Process parent;

	private boolean isRollbacking;
	private RollbackReason reason;

	private final List<IProcessComponentListener> listener;

	protected ProcessComponent() {
		this.id = UUID.randomUUID().toString();
		this.progress = 0.0;
		this.state = ProcessState.READY;

		listener = new ArrayList<IProcessComponentListener>();
	}

	@Override
	public IProcessComponent start() throws InvalidProcessStateException {
		logger.trace("Executing '{}'.", this.getClass().getSimpleName());

		if (state != ProcessState.READY) {
			throw new InvalidProcessStateException(state);
		}
		state = ProcessState.RUNNING;
		isRollbacking = false;

		try {
			doExecute();
			succeed();
		} catch (ProcessExecutionException e) {
			cancel(e.getRollbackReason());
		}

		return this;
	}

	@Override
	public void pause() throws InvalidProcessStateException {
		if (state != ProcessState.RUNNING && state != ProcessState.ROLLBACKING) {
			throw new InvalidProcessStateException(state);
		}
		state = ProcessState.PAUSED;
		doPause();
	}

	@Override
	public void resume() throws InvalidProcessStateException {
		if (state != ProcessState.PAUSED) {
			throw new InvalidProcessStateException(state);
		}
		// TODO don't distinguish between running and rollback state, each component should be able to decide
		// itself (decorators must implement both methods but cannot decide, they can just forward resume())
		if (!isRollbacking) {
			state = ProcessState.RUNNING;
			doResumeExecution();
		} else {
			state = ProcessState.ROLLBACKING;
			doResumeRollback();
		}
	}

	@Override
	public void cancel(RollbackReason reason) throws InvalidProcessStateException {
		if (state != ProcessState.RUNNING && state != ProcessState.PAUSED && state != ProcessState.SUCCEEDED) {
			throw new InvalidProcessStateException(state);
		}

		// inform parent (if exists and not informed yet)
		if (parent != null && parent.getState() != ProcessState.ROLLBACKING) {
			getParent().cancel(reason);
		} else {

			// no parent, or called from parent
			state = ProcessState.ROLLBACKING;
			logger.warn("Rolling back '{}'. Reason: '{}'.", this.getClass().getSimpleName(), reason.getHint());

			doRollback(reason);
		}

		fail(reason);
	}

	/**
	 * Template method responsible for the {@link ProcessComponent} execution.</br>
	 * If a failure is detected, a {@link ProcessExecutionException} is thrown and the component and its
	 * enclosing process component composite, if any, get cancelled and rolled back.
	 * 
	 * @throws InvalidProcessStateException If the component is in an invalid state for this operation.
	 * @throws ProcessExecutionException If a failure is detected during the execution.
	 */
	protected abstract void doExecute() throws InvalidProcessStateException, ProcessExecutionException;

	/**
	 * Template method responsible for the {@link ProcessComponent} pausing.
	 */
	protected abstract void doPause() throws InvalidProcessStateException;

	/**
	 * Template method responsible for the {@link ProcessComponent} execution resume.
	 * 
	 * @throws InvalidProcessStateException If the component is in an invalid state for this operation.
	 */
	protected abstract void doResumeExecution() throws InvalidProcessStateException;

	/**
	 * Template method responsible for the {@link ProcessComponent} rollback resume.
	 */
	protected abstract void doResumeRollback() throws InvalidProcessStateException;

	/**
	 * Template method responsible for the {@link ProcessComponent} rollback.
	 * 
	 * @param reason The reason of the cancellation or fail.
	 * @throws InvalidProcessStateException If the component is in an invalid state for this operation.
	 */
	protected abstract void doRollback(RollbackReason reason) throws InvalidProcessStateException;

	/**
	 * If in {@link ProcessState#RUNNING}, this {@link ProcessComponent} succeeds, changes its state to
	 * {@link ProcessState#SUCCEEDED} and notifies all interested listeners.
	 */
	protected void succeed() {
		if (state == ProcessState.RUNNING) {
			state = ProcessState.SUCCEEDED;
			notifySucceeded();
		}
	}

	/**
	 * If in {@link ProcessState#ROLLBACKING}, this {@link ProcessComponent} succeeds, changes its state to
	 * {@link ProcessState#FAILED} and notifies all interested listeners.
	 */
	protected void fail(RollbackReason reason) {
		if (state == ProcessState.ROLLBACKING) {
			state = ProcessState.FAILED;
			this.reason = reason;
			notifyFailed(reason);
		}
	}

	protected void setParent(Process parent) {
		this.parent = parent;
	}

	protected Process getParent() {
		return parent;
	}

	@Override
	public void await() throws InterruptedException {
		await(-1);
	}

	@Override
	public void await(long timeout) throws InterruptedException {
		if (state == ProcessState.SUCCEEDED || state == ProcessState.FAILED) {
			return;
		}

		final CountDownLatch latch = new CountDownLatch(1);

		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		ScheduledFuture<?> handle = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (state == ProcessState.SUCCEEDED || state == ProcessState.FAILED) {
					latch.countDown();
				}
			}
		}, 0, 100, TimeUnit.MILLISECONDS);

		// blocking wait for completion or interruption
		try {
			if (timeout < 0) {
				latch.await();
			} else {
				boolean success = latch.await(timeout, TimeUnit.MILLISECONDS);
				if (!success) {
					throw new InterruptedException("Waiting for process timed out.");
				}
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted while waiting for process.", e);
			throw e;
		} finally {
			handle.cancel(true);
		}
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public double getProgress() {
		return progress;
	}

	@Override
	public ProcessState getState() {
		return state;
	}

	public synchronized void attachListener(IProcessComponentListener listener) {
		this.listener.add(listener);

		// TODO check if correct
		// if process component completed already
		if (state == ProcessState.SUCCEEDED) {
			listener.onSucceeded();
		} else if (state == ProcessState.FAILED) {
			listener.onFailed(reason);
		}
	}

	public synchronized void detachListener(IProcessComponentListener listener) {
		this.listener.remove(listener);
	}

	@Override
	public List<IProcessComponentListener> getListener() {
		return listener;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (!(obj instanceof ProcessComponent)) {
			return false;
		}

		ProcessComponent other = (ProcessComponent) obj;
		return id.equals(other.getID());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		return 31 * hash + id.hashCode();
	}

	private void notifySucceeded() {
		for (IProcessComponentListener listener : this.listener) {
			listener.onSucceeded();
		}
	}

	private void notifyFailed(RollbackReason reason) {
		for (IProcessComponentListener listener : this.listener) {
			listener.onFailed(reason);
		}
	}

}
