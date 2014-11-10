package org.hive2hive.core.exceptions;

public class AbortModifyException extends Hive2HiveException {

	private static final long serialVersionUID = 492292641365823866L;

	public AbortModifyException() {
		this("Modification failed");
	}

	public AbortModifyException(String message) {
		super(message);
	}
}
