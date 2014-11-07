package org.hive2hive.processframework;

import org.hive2hive.core.exceptions.ProcessError;

/**
 * Represents the reason of a process component failure and might indicate why it got cancelled and rolled
 * back.
 * 
 * @author Christian
 * 
 */
public class RollbackReason {

	private final String hint;
	private final Exception cause;
	private final ProcessError errorType;

	public RollbackReason(String hint) {
		this(hint, null);
	}

	public RollbackReason(String hint, Exception cause) {
		this(hint, cause, ProcessError.FAILED);
	}
	
	public RollbackReason(String hint, Exception cause, ProcessError errorType) {
		this.hint = hint;
		this.cause = cause;
		this.errorType = errorType;
	}

	/**
	 * Getter for the hint.
	 * 
	 * @return A hint about why the process component has failed.
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * Getter for the cause.
	 * 
	 * @return The cause (or exception) that caused the process component to fail, if any.
	 */
	public Exception getCause() {
		return cause;
	}

	
	public ProcessError getErrorType(){
		return errorType;
	}
}
