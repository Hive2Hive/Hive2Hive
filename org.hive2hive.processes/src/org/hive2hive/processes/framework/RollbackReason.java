package org.hive2hive.processes.framework;

import org.hive2hive.processes.framework.abstracts.ProcessComponent;

public class RollbackReason {

	private final ProcessComponent component;
	private final String message;

	public RollbackReason(ProcessComponent component, String message) {
		this.component = component;
		this.message = message;
	}

	public ProcessComponent getComponent() {
		return component;
	}

	public String getMessage() {
		return message;
	}

}
