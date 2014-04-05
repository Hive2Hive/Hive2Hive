package org.hive2hive.core.exceptions;

public class GetFailedException extends Hive2HiveException {

	private static final long serialVersionUID = -676084733761214493L;

	public GetFailedException() {
		this("Getting content from the DHT failed.");
	}

	public GetFailedException(String message) {
		super(message);
	}
}
