package org.hive2hive.core.exceptions;

public class ParentInUserProfileNotFoundException extends Exception {

	private static final long serialVersionUID = 8193285253828289421L;

	public ParentInUserProfileNotFoundException(Throwable cause) {
		super(cause);
	}
	
	public ParentInUserProfileNotFoundException(String hint) {
		super(hint);
	}
	
}
