package org.hive2hive.core.exceptions;

public abstract class Hive2HiveException extends Exception {

	private static final long serialVersionUID = 1165145046548713366L;
	private ErrorCode error;
	public Hive2HiveException() {
		super("Something is wrong with a Hive2Hive operation.");
	}
	

	public Hive2HiveException(String message) {
		super(message);
	}
	
	public Hive2HiveException(ErrorCode error, String message) {
		super(message);
		this.error = error;
	}
	
	public ErrorCode getError(){
		return error;
	}
}
