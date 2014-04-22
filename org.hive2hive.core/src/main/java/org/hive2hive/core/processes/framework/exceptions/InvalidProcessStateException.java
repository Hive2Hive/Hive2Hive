package org.hive2hive.core.processes.framework.exceptions;

import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.processes.framework.ProcessState;

/**
 * Represents an exception that occurs when a process component is in an invalid state to be called for a
 * specific operation (e.g., cannot be paused because it is not even running yet).
 * 
 * @author Christian
 * 
 */
public class InvalidProcessStateException extends Hive2HiveException {

	private static final long serialVersionUID = -570684360354374306L;

	private final ProcessState current;

	public InvalidProcessStateException(ProcessState current) {
		super(String.format("Operation cannot be called. Process is currently in an invalid state: %s.",
				current));
		this.current = current;
	}

	public ProcessState getCurrentState() {
		return current;
	}
}
