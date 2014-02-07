package org.hive2hive.core.processes.framework;

public class RollbackReason {

	private final String hint;
	private final Throwable cause;
	
	public RollbackReason(String hint) {
		this(hint, null);
	}
	
	public RollbackReason(String hint, Throwable cause) {
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
