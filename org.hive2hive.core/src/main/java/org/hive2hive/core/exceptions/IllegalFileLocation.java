package org.hive2hive.core.exceptions;

public class IllegalFileLocation extends Hive2HiveException {

	private static final long serialVersionUID = 551397781677812751L;

	public IllegalFileLocation() {
		this("File location is not valid");
	}

	public IllegalFileLocation(String message) {
		super(message);
	}
}
