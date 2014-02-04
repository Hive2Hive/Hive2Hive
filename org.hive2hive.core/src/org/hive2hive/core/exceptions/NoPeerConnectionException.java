package org.hive2hive.core.exceptions;

public class NoPeerConnectionException extends Hive2HiveException {

	private static final long serialVersionUID = -6268490856844206418L;

	public NoPeerConnectionException() {
		super("The peer is not connected");
	}
}
