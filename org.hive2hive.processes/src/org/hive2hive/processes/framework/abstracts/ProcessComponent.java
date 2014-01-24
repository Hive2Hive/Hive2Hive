package org.hive2hive.processes.framework.abstracts;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.processes.framework.ProcessState;
import org.hive2hive.processes.framework.ProcessUtil;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;

public abstract class ProcessComponent implements IProcessComponent {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ProcessComponent.class);

	private final String id;
	private double progress;
	private ProcessState state;

	private boolean isRollbacking;

	private Process parent;

	private final List<IProcessComponentListener> listener;

	protected ProcessComponent() {
		this.id = ProcessUtil.generateID();
		this.progress = 0.0;
		this.state = ProcessState.READY;

		listener = new ArrayList<IProcessComponentListener>();
	}

	@Override
	public final void start() throws InvalidProcessStateException {
		if (state != ProcessState.READY) {
			throw new InvalidProcessStateException(state);
		}
		state = ProcessState.RUNNING;
		isRollbacking = false;

		logger.debug(String.format("Executing '%s'.", this.getClass().getSimpleName()));
		doExecute();

		// TODO set leafs to succeeded when composite succeeded
		if (state == ProcessState.RUNNING && parent == null) {
			state = ProcessState.SUCCEEDED;
			notifySucceeded();
		}
	}

	@Override
	public final void pause() throws InvalidProcessStateException {
		if (state != ProcessState.RUNNING && state != ProcessState.ROLLBACKING) {
			throw new InvalidProcessStateException(state);
		}
		state = ProcessState.PAUSED;
		doPause();
	}

	@Override
	public final void resume() throws InvalidProcessStateException {
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
	public final void cancel(RollbackReason reason) throws InvalidProcessStateException {
		logger.error("Cancel called. Reason: " + reason.getMessage());
		if (state != ProcessState.RUNNING && state != ProcessState.PAUSED) {
			throw new InvalidProcessStateException(state);
		}

		// inform parent (if it isn't yet)
		if (parent != null && parent.getState() != ProcessState.ROLLBACKING) {
			getParent().cancel(reason);
		} else {

			// no parent or called from parent
			state = ProcessState.ROLLBACKING;
			logger.debug(String.format("Rolling back '%s'. Reason: %s", this.getClass().getSimpleName(),
					reason.getMessage()));

			doRollback(reason);
		}

		// TODO set leafs to failed when composite failed
		if (state == ProcessState.ROLLBACKING && parent == null) {
			state = ProcessState.FAILED;
			notifyFailed();
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

	public Process getParent() {
		return parent;
	}

	public void setParent(Process parent) {
		this.parent = parent;
	}

	@Override
	public abstract void join();

	protected abstract void doExecute() throws InvalidProcessStateException;

	protected abstract void doPause();

	protected abstract void doResumeExecution() throws InvalidProcessStateException;

	protected abstract void doResumeRollback();

	protected abstract void doRollback(RollbackReason reason) throws InvalidProcessStateException;

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

	protected void notifySucceeded() {
		for (IProcessComponentListener listener : this.listener) {
			listener.onSucceeded();
		}
		notifyFinished();
	}

	protected void notifyFailed() {
		for (IProcessComponentListener listener : this.listener) {
			listener.onFailed();
		}
		notifyFinished();
	}

	private void notifyFinished() {
		for (IProcessComponentListener listener : this.listener) {
			listener.onFinished();
		}
	}

}
