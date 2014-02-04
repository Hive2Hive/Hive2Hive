package org.hive2hive.core.processes.framework.exceptions;

import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.processes.framework.ProcessState;

public class InvalidProcessStateException extends Hive2HiveException {

	private static final long serialVersionUID = -570684360354374306L;

	public InvalidProcessStateException(ProcessState current) {
		super(String.format("Operation cannot be called. Process is currently in an invalid state: %s.",
				current));
	}
}
