package org.hive2hive.core.exceptions;

public class UserAlreadyRegisteredException extends Hive2HiveException {

	private static final long serialVersionUID = -1897705737474342731L;
	
	public UserAlreadyRegisteredException(String userId) {
		super(String.format("The user '%s' is already registered and cannot be registered again.", userId));
	}
}
