package org.hive2hive.core.exceptions;

public class PutFailedException extends Hive2HiveException {

	private static final long serialVersionUID = -676084733761214493L;

	public PutFailedException() {
		this("Putting content to the DHT failed.");
	}

	public PutFailedException(String message) {
		super(message);
	}
}
