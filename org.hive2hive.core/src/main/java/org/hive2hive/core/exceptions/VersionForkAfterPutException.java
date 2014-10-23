package org.hive2hive.core.exceptions;

public class VersionForkAfterPutException extends PutFailedException{

	private static final long serialVersionUID = 7977352321747977256L;

	public VersionForkAfterPutException() {
		super("Version fork after put detected.");
	}

}
