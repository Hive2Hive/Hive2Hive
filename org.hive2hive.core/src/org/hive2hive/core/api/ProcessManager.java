package org.hive2hive.core.api;

import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public class ProcessManager {

	private final boolean autostart;

	public ProcessManager(boolean autostart) {
		this.autostart = autostart;
		
	}

	public void submit(IProcessComponent registerProcess) {
		// TODO Auto-generated method stub
		// TODO auto start if configured
		
	}
}
