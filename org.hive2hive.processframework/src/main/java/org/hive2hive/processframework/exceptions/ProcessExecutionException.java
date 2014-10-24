package org.hive2hive.processframework.exceptions;

import org.hive2hive.processframework.ProcessError;
import org.hive2hive.processframework.RollbackReason;

/**
 * Exception that occurs during a process component execution in case of a failure. Leads to the whole process
 * composite to be cancelled and rolled back.
 * 
 * @author Christian
 * 
 */
public class ProcessExecutionException extends Exception {

	private static final long serialVersionUID = -107686918145129011L;

	private final RollbackReason reason;

	public ProcessExecutionException(Exception cause, ProcessError failType) {
		this(new RollbackReason(cause.getMessage(), cause, failType));
	}
	
	public ProcessExecutionException(Exception cause) {
		this(new RollbackReason(cause.getMessage(), cause, ProcessError.FAILED));
	}

	public ProcessExecutionException(String hint) {
		this(new RollbackReason(hint, null));
	}
	
	public ProcessExecutionException(String hint, ProcessError failType) {
		this(new RollbackReason(hint, null, failType));
	}

	public ProcessExecutionException(String hint, Exception cause) {
		this(new RollbackReason(hint, cause));
	}

	public ProcessExecutionException(RollbackReason reason) {
		this.reason = reason;
	}

	public RollbackReason getRollbackReason() {
		return reason;
	}
}
