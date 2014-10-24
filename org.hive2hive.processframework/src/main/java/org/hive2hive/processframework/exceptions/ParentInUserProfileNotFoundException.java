package org.hive2hive.processframework.exceptions;

import org.hive2hive.processframework.ProcessError;
import org.hive2hive.processframework.RollbackReason;
public class ParentInUserProfileNotFoundException extends ProcessExecutionException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8193285253828289421L;

	public ParentInUserProfileNotFoundException(Exception cause) {
		super(cause, ProcessError.PARENT_IN_USERFILE_NOT_FOUND);
		// TODO Auto-generated constructor stub
	}
	
	public ParentInUserProfileNotFoundException(String hint) {
		super(hint, ProcessError.PARENT_IN_USERFILE_NOT_FOUND);
	}
	
}
