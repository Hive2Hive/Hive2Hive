package org.hive2hive.core.exceptions;

public class SendFailedException extends Hive2HiveException {

	private static final long serialVersionUID = 226923782989779245L;

	public SendFailedException() {
		this("The message could not be sent");
	}

	public SendFailedException(String message) {
		super(message);
	}
}
