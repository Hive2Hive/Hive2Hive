package org.hive2hive.processes.framework.interfaces;

public interface IRollbackable {

	public void rollback();
	
	public boolean isRollbacking();
}
