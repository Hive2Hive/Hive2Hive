package org.hive2hive.core.flowcontrol;


/**
 * One step of a complete workflow. This step calls the next step after finishing
 * 
 * @author Nendor, Nico
 * 
 */
public abstract class ProcessStep {

	private Process process;

	public void setProcess(Process aProcess) {
		process = aProcess;
	}

	protected Process getProcess() {
		return process;
	}

	/**
	 * Called by the containing process to tell this step to start with its work.
	 */
	public abstract void start();

	/**
	 * Tells this step to undo any work it did previously. If this step changed anything in the network it
	 * needs to be revoked completely. After the execution of this method the global state of the network
	 * needs to be the same as if this step never existed.
	 */
	public abstract void rollBack();

}
