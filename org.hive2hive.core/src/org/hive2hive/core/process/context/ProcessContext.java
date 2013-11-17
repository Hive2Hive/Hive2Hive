package org.hive2hive.core.process.context;

import org.hive2hive.core.process.Process;

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
