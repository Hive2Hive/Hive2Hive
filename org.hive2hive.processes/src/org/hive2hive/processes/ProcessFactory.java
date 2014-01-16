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
	
	public IProcessComponent createRegisterProcess() {
		
		SequentialProcess registerProcess = new SequentialProcess();
//		registerProcess.add(new CheckIfUserExistsStep());
		
		return registerProcess;
	}
}
