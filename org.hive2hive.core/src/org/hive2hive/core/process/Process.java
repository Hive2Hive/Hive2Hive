package org.hive2hive.core.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;
import org.hive2hive.core.process.listener.IProcessListener;
import org.hive2hive.core.process.manager.ProcessManager;

/**
 * This abstract process is used for executing workflows. It keeps the order of the process steps.
 * This wrapper is necessary since workflows contain many long-running network calls. The callback of
 * then starts the next process step
 * 
 * @author Nendor, Nico, Christian
 * 
 */
public abstract class Process implements IProcess {

	// TODO throw exceptions on invalid process states

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(Process.class);

	private final NetworkManager networkManager;
	private final int pid;
	private ProcessState state;

	private ProcessStep currentStep;

	private final List<ProcessStep> executedSteps = new ArrayList<ProcessStep>();
	private final List<IProcessListener> listeners = new ArrayList<IProcessListener>();

	public Process(NetworkManager networkManager) {
		this.networkManager = networkManager;
		this.pid = ProcessManager.getInstance().getNewPID();
		this.state = ProcessState.INITIALIZING;

		ProcessManager.getInstance().attachProcess(this);
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
			new Thread(this).start();
		} else {
			logger.error("Process state is " + state.toString() + ". Cannot start.");
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
		if (state != ProcessState.STOPPED && state != ProcessState.ROLLBACKING) {
			// first roll back
			rollBack(reason);

			// then mark process as stopped
			state = ProcessState.STOPPED;
		} else {
			logger.warn("Process is already stopped or still rollbacking");
		}
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

	@Override
	public final void run() {
		if (currentStep != null) {
			currentStep.start();
		} else {
			logger.error("No process step to start with specified.");
		}
	}

	protected void finish() {
		if (state == ProcessState.RUNNING) {
			state = ProcessState.FINISHED;
			ProcessManager.getInstance().detachProcess(this);

			for (IProcessListener listener : listeners) {
				listener.onSuccess();
			}
		}
	}

	private void rollBack(String reason) {
		state = ProcessState.ROLLBACKING;
		logger.warn(String.format("Rollback triggered. Reason = '%s'", reason));

		// roll back current step
		currentStep.rollBack();

		// rollback already executed steps
		Collections.reverse(executedSteps);
		for (ProcessStep step : executedSteps) {
			step.rollBack();
		}

		for (IProcessListener listener : listeners) {
			listener.onFail(reason);
		}
	}

	/**
	 * Get the {@link NetworkManager} which is hosting this process.
	 * 
	 * @return Returns the hosting {@link NetworkManager}.
	 */
	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	public void addListener(IProcessListener listener) {
		listeners.add(listener);
	}

	public boolean removeListener(IProcessListener listener) {
		return listeners.remove(listener);
	}
}
