package org.hive2hive.core.exceptions;

import org.hive2hive.processframework.ProcessError;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public class PutToDHTException extends ProcessExecutionException{
//ProcessExecutionException{


	/**
	 * 
	 */
	private static final long serialVersionUID = -7119515308918446698L;

	public PutToDHTException(Exception cause) {
		super(cause, ProcessError.PUT_FAILED);
		// TODO Auto-generated constructor stub
	}
	
	public PutToDHTException(String hint) {
		super(hint, ProcessError.VERSION_FORK);
	}
}
