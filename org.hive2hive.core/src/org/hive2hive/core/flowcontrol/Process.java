package org.hive2hive.core.flowcontrol;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.flowcontrol.manager.ProcessManager;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.NetworkManager;

/**
 * This abstract process is used for executing workflows. It keeps the order of the process steps.
 * This wrapper is necessary since workflows contain many long-running network calls. The callback of
 * then starts the next process step
 * 
 * @author Nendor, Nico
 * 
 */
public abstract class Process implements Runnable, IProcess {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(NetworkManager.class);

	private final int pid;
	private ProcessState state = ProcessState.INITIALIZING;
	private final NetworkManager networkManager;
	private ProcessStep currentStep;
	private final List<ProcessStep> executedSteps = new ArrayList<ProcessStep>();

	public Process(NetworkManager networkManager, ProcessStep firstStep) {
		this.networkManager = networkManager;
		ProcessManager processManager = ProcessManager.getInstance();
		pid = processManager.getIdForNewProcess();
		processManager.attachProcess(this);

		currentStep = firstStep;
		currentStep.setProcess(this);
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
			logger.error("Process is already stopped");
		} else {
			state = ProcessState.STOPPED;
			rollBack();
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

	protected void rollBack() {
		for (ProcessStep step : executedSteps) {
			step.rollBack();
		}
	}
}
