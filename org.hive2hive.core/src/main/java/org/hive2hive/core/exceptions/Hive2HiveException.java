package org.hive2hive.core.exceptions;

public class Hive2HiveException extends Exception {

	private static final long serialVersionUID = 1165145046548713366L;

	public Hive2HiveException() {
		super("Something is wrong with a Hive2Hive operation.");
	}

	public Hive2HiveException(String message) {
		super(message);
	}
}
