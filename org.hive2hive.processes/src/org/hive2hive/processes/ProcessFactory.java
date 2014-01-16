package org.hive2hive.processes;


public final class ProcessFactory {

	private static ProcessFactory instance;
	
	public static ProcessFactory instance() {
		if (instance == null)
			instance = new ProcessFactory();
		return instance;
	}
	
	private ProcessFactory() {
	}
	
//	public IProcessComponent createRegisterProcess() {
//		
//	}
}
