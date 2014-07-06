package org.hive2hive.processframework;

/**
 * Represents the reason of a process component failure and might indicate why it got cancelled and rolled
 * back.
 * 
 * @author Christian
 * 
 */
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
	public Throwable getCause() {
		return cause;
	}
}
