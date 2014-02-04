package org.hive2hive.core.processes.framework.exceptions;

public class ProcessExecutionException extends Exception {

	private static final long serialVersionUID = -107686918145129011L;

	private final String hint;
	private final Throwable cause;

	public ProcessExecutionException(Throwable cause) {
		this(cause.getMessage(), cause);
	}
	
	public ProcessExecutionException(String hint) {
		this(hint, null);
	}
	
	public ProcessExecutionException(String hint, Throwable cause) {
		this.hint = hint;
		this.cause = cause;
	}
	
	public String getHint() {
		return hint;
	}

	public Throwable getCause() {
		return cause;
	}
}
