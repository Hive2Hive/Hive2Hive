package org.hive2hive.core.processes.framework.abstracts;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.processes.framework.ProcessState;
import org.hive2hive.core.processes.framework.ProcessUtil;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponentListener;

public abstract class ProcessComponent implements IProcessComponent {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ProcessComponent.class);

	private final String id;
	private double progress;
	private ProcessState state;
	private Process parent;

	private boolean isRollbacking;


	private final List<IProcessComponentListener> listener;

	protected ProcessComponent() {
		this.id = ProcessUtil.generateID();
		this.progress = 0.0;
		this.state = ProcessState.READY;

		listener = new ArrayList<IProcessComponentListener>();
	}

	@Override
	public void start() throws InvalidProcessStateException, ProcessExecutionException {
		logger.debug(String.format("Executing '%s'.", this.getClass().getSimpleName()));

		if (state != ProcessState.READY) {
			throw new InvalidProcessStateException(state);
		}
		state = ProcessState.RUNNING;
		isRollbacking = false;

		try {
			doExecute();
		} catch (ProcessExecutionException e) {
			// TODO initiate rollback
			cancel(new RollbackReason(e));
			
			// TODO return the exception
			throw e;
		}

		succeed();
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
		logger.warn(String.format("Cancelling '%s'. Reason: %s", this.getClass().getSimpleName(),
				reason.getException().getMessage()));

		if (state != ProcessState.RUNNING && state != ProcessState.PAUSED && state != ProcessState.SUCCEEDED) {
			throw new InvalidProcessStateException(state);
		}

		// inform parent (if exists and not informed yet)
		if (parent != null && parent.getState() != ProcessState.ROLLBACKING) {
			getParent().cancel(reason);
		} else {

			// no parent, or called from parent
			state = ProcessState.ROLLBACKING;
			logger.debug(String.format("Rolling back '%s'. Reason: %s", this.getClass().getSimpleName(),
					reason.getException().getMessage()));

			// TODO wait for doExecute() to complete
			doRollback(reason);
		}

		fail(reason);
	}

	protected abstract void doExecute() throws InvalidProcessStateException, ProcessExecutionException;

	protected abstract void doPause();

	protected abstract void doResumeExecution() throws InvalidProcessStateException;

	protected abstract void doResumeRollback();

	protected abstract void doRollback(RollbackReason reason) throws InvalidProcessStateException;

	protected void succeed() {
		if (state == ProcessState.RUNNING) {
			state = ProcessState.SUCCEEDED;
			notifySucceeded();
		}
	}

	protected void fail(RollbackReason reason) {
		if (state == ProcessState.ROLLBACKING) {
			state = ProcessState.FAILED;
			notifyFailed(reason);
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

	public void setParent(Process parent) {
		this.parent = parent;
	}

	public Process getParent() {
		return parent;
	}

	public void attachListener(IProcessComponentListener listener) {
		this.listener.add(listener);
	}

	public void detachListener(IProcessComponentListener listener) {
		this.listener.remove(listener);
	}

	public List<IProcessComponentListener> getListener() {
		return listener; // TODO copy before return?
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof ProcessComponent))
			return false;

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
		notifyFinished();
	}

	private void notifyFailed(RollbackReason reason) {
		for (IProcessComponentListener listener : this.listener) {
			listener.onFailed(reason);
		}
		notifyFinished();
	}

	private void notifyFinished() {
		for (IProcessComponentListener listener : this.listener) {
			listener.onFinished();
		}
	}

}
