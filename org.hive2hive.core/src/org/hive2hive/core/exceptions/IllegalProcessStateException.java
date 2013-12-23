package org.hive2hive.core.exceptions;

import org.hive2hive.core.process.ProcessState;

public class IllegalProcessStateException extends Hive2HiveException {

	private static final long serialVersionUID = -7104744691294564567L;

	public IllegalProcessStateException() {
		super("The operation cannot be called because the process is in another state");
	}

	public IllegalProcessStateException(String message) {
		super(message);
	}

	public IllegalProcessStateException(String message, ProcessState currentState) {
		super(message + " Current state: " + currentState.name());
	}
}
