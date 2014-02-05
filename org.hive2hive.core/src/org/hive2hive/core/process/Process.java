package org.hive2hive.core.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.exceptions.IllegalProcessStateException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.context.ProcessContext;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.processes.implementations.notify.BaseNotificationMessageFactory;

/**
 * This abstract process is used for executing workflows. It keeps the order of the process steps.
 * This wrapper is necessary since workflows contain many long-running network calls. The callback of
 * then starts the next process step
 * 
 * @author Nendor, Nico, Christian
 * 
 */
public abstract class Process implements IProcess {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(Process.class);

	private final NetworkManager networkManager;
	private final int pid;
	private ProcessState state;
	private ProcessStep currentStep;

	private final List<ProcessStep> executedSteps = new ArrayList<ProcessStep>();
	private final List<IProcessListener> listeners = new ArrayList<IProcessListener>();
	private Thread thread;

	public Process(NetworkManager networkManager) {
		this.networkManager = networkManager;
		this.pid = ProcessManager.getInstance().getNewPID();
		this.state = ProcessState.INITIALIZING;
	}

	/**
	 * Sets the next {@link ProcessStep} of this process and starts executing if this process is in
	 * {@link ProcessState#RUNNING}. Otherwise the next step will be marked to be executed when the process
	 * resumes.
	 * 
	 * @param nextStep the next step or <code>null</code>, if the process should finish
	 */
	public void setNextStep(ProcessStep nextStep) {
		if (currentStep != null)
			executedSteps.add(currentStep);

		if (nextStep != null) {
			currentStep = nextStep;
			currentStep.setProcess(this);

			if (state == ProcessState.RUNNING)
				currentStep.start();
		} else {
			finish();
		}
	}

	@Override
	public void start() {
		if (state == ProcessState.INITIALIZING) {
			state = ProcessState.RUNNING;
			ProcessManager.getInstance().attachProcess(this);
			thread = new Thread(this);
			thread.start();
		} else {
			logger.error("Process state is " + state.toString() + ". Cannot start.");
		}
	}

	@Override
	public void run() {
		if (currentStep != null) {
			currentStep.start();
		} else {
			logger.warn("No process step to start with specified.");
			finish();
		}
	}

	@Override
	public void pause() {
		if (state == ProcessState.RUNNING) {
			state = ProcessState.PAUSED;
		} else {
			logger.error("Process state is " + state.toString() + ". Cannot pause.");
		}
	}

	@Override
	public void continueProcess() {
		if (state == ProcessState.PAUSED) {
			state = ProcessState.RUNNING;
			if (currentStep != null) {
				currentStep.start();
			} else {
				logger.error("No step to continue.");
			}
		} else {
			logger.error("Process state is " + state.toString() + ". Cannot continue.");
		}
	}

	@Override
	public void stop(String reason) {
		stop(new Hive2HiveException(reason));
	}

	@Override
	public void stop(Exception exception) {
		if (state != ProcessState.STOPPED && state != ProcessState.ROLLBACKING) {
			logger.error(this.getClass().getSimpleName() + " stopped. Reason: " + exception.getMessage());
			// start roll back
			rollBack(exception);
		} else {
			logger.warn("Process is already stopped or still rollbacking");
		}
	}

	public void join() throws InterruptedException, IllegalProcessStateException {
		if (state == ProcessState.RUNNING) {
			if (thread != null) {
				thread.join();
			}
		} else {
			logger.warn("Cannot join the process since it's not running");
			throw new IllegalProcessStateException("The process is not running.", state);
		}
	}

	@Override
	public void terminate() {
		setNextStep(null);
	}

	@Override
	public final int getID() {
		return pid;
	}

	@Override
	public final ProcessState getState() {
		return state;
	}

	@Override
	public final int getProgress() {
		return executedSteps.size();
	}

	/**
	 * Returns the process context. This methods should override this method and covariantly narrow down the
	 * return type.
	 */
	public ProcessContext getContext() {
		return null;
	}

	private void finish() {
		if (state == ProcessState.RUNNING) {
			state = ProcessState.FINISHED;
			ProcessManager.getInstance().detachProcess(this);

			for (IProcessListener listener : listeners) {
				listener.onSuccess();
			}
		}
	}

	// TODO redesign this here
	private Exception rollbackException;

	private void rollBack(Exception exception) {
		state = ProcessState.ROLLBACKING;
		rollbackException = exception;
		logger.warn(String.format("Rollback triggered. Reason = '%s'", exception));

		// start roll back from current step
		if (currentStep != null)
			currentStep.rollBack();
	}

	public void nextRollBackStep() {
		if (ProcessState.ROLLBACKING == state) {
			if (!executedSteps.isEmpty()) {
				// get last executed element
				ProcessStep step = executedSteps.remove(executedSteps.size() - 1);
				// trigger rollback for this step
				step.rollBack();
			} else {
				// mark process as stopped
				state = ProcessState.STOPPED;
				ProcessManager.getInstance().detachProcess(this);
				// notify listeners about rollbacking
				for (IProcessListener listener : listeners) {
					listener.onFail(rollbackException);
				}
			}
		}
	}

	/**
	 * Notify the other clients of the same users, other users, ... Configure this in the
	 * {@link BaseNotificationMessageFactory}
	 */
	@Deprecated
	public void sendNotification(BaseNotificationMessageFactory messageFactory, Set<String> users) {
		// NotifyPeersProcess notifyProcess = new NotifyPeersProcess(getNetworkManager(), messageFactory,
		// users);
		// notifyProcess.start();
	}

	/**
	 * Get the {@link NetworkManager} which is hosting this process.
	 * 
	 * @return Returns the hosting {@link NetworkManager}.
	 */
	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	@Override
	public void addListener(IProcessListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean removeListener(IProcessListener listener) {
		return listeners.remove(listener);
	}

	public List<IProcessListener> getListeners() {
		return listeners;
	}
}
