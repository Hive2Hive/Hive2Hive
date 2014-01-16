package org.hive2hive.processes.framework.exceptions;

import org.hive2hive.processes.framework.ProcessState;

public class InvalidProcessStateException extends Exception {

	private static final long serialVersionUID = -570684360354374306L;

	public InvalidProcessStateException(ProcessState current) {
		super(String.format("Operation cannot be called. Process is currently in an invalid state: %s.", current));
	}
}
