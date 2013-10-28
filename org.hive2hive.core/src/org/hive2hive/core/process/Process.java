package org.hive2hive.core.process;

import java.util.ArrayList;
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
 * @author Nendor, Nico
 * 
 */
public abstract class Process implements IProcess {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(NetworkManager.class);

	private final int pid;
	private ProcessState state = ProcessState.INITIALIZING;
	private final NetworkManager networkManager;
	private ProcessStep firstStep;
	private ProcessStep currentStep;
	private final List<ProcessStep> executedSteps = new ArrayList<ProcessStep>();
	private final List<IProcessListener> listeners = new ArrayList<IProcessListener>();

	public Process(NetworkManager networkManager) {
		this.networkManager = networkManager;
		ProcessManager processManager = ProcessManager.getInstance();
		pid = processManager.getIdForNewProcess();
		processManager.attachProcess(this);
	}

	public void setFirstStep(ProcessStep firstStep) {
		firstStep.setProcess(this);
		this.firstStep = firstStep;
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
			// finish the process step; Process will pause before next step
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
				logger.error("No next step to continue");
			}
		} else {
			logger.error("Process state is " + state.toString() + ". Cannot continue.");
		}
	}

	@Override
	public void stop() {
		if (state == ProcessState.STOPPED) {
			logger.warn("Process is already stopped");
		} else {
			state = ProcessState.STOPPED;
			rollBack("Process stopped.");
		}
	}

	@Override
	public int getProgress() {
		return executedSteps.size();
	}

	@Override
	public int getID() {
		return pid;
	}

	@Override
	public void run() {
		currentStep = firstStep;
		currentStep.start();
	}

	@Override
	public ProcessState getState() {
		return state;
	}

	/**
	 * Calls the next step
	 * 
	 * @param nextStep if null, the process will finalize itself and be done. Else, the given process step
	 *            will be executed
	 */
	public void nextStep(ProcessStep nextStep) {
		executedSteps.add(currentStep);

		if (nextStep == null) {
			// implicitly finalizing the process
			finalize();
		} else {
			// normal case
			currentStep = nextStep;
			currentStep.setProcess(this);

			if (state == ProcessState.RUNNING)
				currentStep.start();
		}
	}

	/**
	 * When all steps of this process are finished
	 */
	public void finalize() {
		if (state == ProcessState.RUNNING) {
			state = ProcessState.FINISHED;
			// detach form the process manager
			ProcessManager.getInstance().detachProcess(this);
			for (IProcessListener listener : listeners) {
				listener.onSuccess();
			}
		}
	}

	/**
	 * Getter
	 * 
	 * @return the {@link NetworkManager} which hosts this process.
	 */
	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	public void rollBack(String reason) {
		state = ProcessState.ROLLBACK;
		logger.warn(String.format("Rollback triggered. Reason = '%s'", reason));
		currentStep.rollBack();

		// TODO: rollback reverse order
		for (ProcessStep step : executedSteps) {
			step.rollBack();
		}

		for (IProcessListener listener : listeners) {
			listener.onFail(reason);
		}
	}

	public void addListener(IProcessListener listener) {
		listeners.add(listener);
	}

	public boolean removeListener(IProcessListener listener) {
		return listeners.remove(listener);
	}
}
