package org.hive2hive.core.exceptions;

public class NoSessionException extends Hive2HiveException {

	private static final long serialVersionUID = 4263677549436609207L;

	public NoSessionException() {
		super("No session found.");
	}
}
