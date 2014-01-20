package org.hive2hive.processes.framework.interfaces;

import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public interface IControllable {

	public void start() throws InvalidProcessStateException;
	
	public void pause() throws InvalidProcessStateException;
	
	public void resume() throws InvalidProcessStateException;
	
	public void cancel(RollbackReason reason) throws InvalidProcessStateException;
}