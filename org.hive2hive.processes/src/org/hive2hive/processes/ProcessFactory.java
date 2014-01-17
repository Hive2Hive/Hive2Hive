package org.hive2hive.processes;

import org.hive2hive.processes.framework.concretes.SequentialProcess;
import org.hive2hive.processes.framework.interfaces.IProcessComponent;


public final class ProcessFactory {

	private static ProcessFactory instance;
	
	public static ProcessFactory instance() {
		if (instance == null)
			instance = new ProcessFactory();
		return instance;
	}
	
	private ProcessFactory() {
	}
	
	// TODO the creation of this process can later on be moved into a factory method of a RegisterProcess class
	public IProcessComponent createRegisterProcess() {
		
		SequentialProcess registerProcess = new SequentialProcess();
//		registerProcess.add(new CheckIfUserExistsStep());
		
		return registerProcess;
	}
}
