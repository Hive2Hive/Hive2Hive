package org.hive2hive.core.processes.framework;

import org.hive2hive.core.processes.framework.exceptions.ProcessExecutionException;

// TODO atm only a wrapper class, might be removed in a later stage

public class RollbackReason {

	private final ProcessExecutionException exception;
	
	public RollbackReason(ProcessExecutionException exception) {
		this.exception = exception;
	}

	public ProcessExecutionException getException() {
		return exception;
	}

}
