package org.hive2hive.core.exceptions;

public class NoNetworkException extends Hive2HiveException {

	private static final long serialVersionUID = 1L;

	public NoNetworkException() {
		super("This component is not attached to the network.");
	}
}
