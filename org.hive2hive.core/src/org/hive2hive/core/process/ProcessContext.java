package org.hive2hive.core.process;

/**
 * The context of a process. Basically the context just holds process-specific data.
 * @author Christian
 *
 */
public abstract class ProcessContext {

	private final Process process;

	public ProcessContext(Process process) {
		this.process = process;
	}
	
	public Process getProcess() {
		return process;
	}
}
