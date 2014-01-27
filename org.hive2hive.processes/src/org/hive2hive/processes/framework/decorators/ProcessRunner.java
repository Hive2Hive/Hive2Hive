package org.hive2hive.processes.framework.decorators;

import java.util.concurrent.Callable;

import org.hive2hive.processes.framework.abstracts.ProcessComponent;

public class ProcessRunner implements Callable<Boolean> {

	private final ProcessComponent component;

	public ProcessRunner(ProcessComponent component) {
		this.component = component;
	}
	
	@Override
	public Boolean call() throws Exception {
		
		try {
			Thread.currentThread().checkAccess();
			Thread.currentThread().setName(
					String.format("async-process %s ", component.getClass().getSimpleName()));
		} catch (SecurityException e) {
		}
		;

		component.start();
		return true;
	}

}
