package org.hive2hive.processframework;

public enum ProcessError {
	
	FAILED, //TODO remove as soon as possible, to general.
	
	PUT_FAILED,
	
	GET_FAILED,
	
	PARENT_IN_USERFILE_NOT_FOUND,
	
	VERSION_FORK //unused atm as PutFailedException throws are not distinguished

}
