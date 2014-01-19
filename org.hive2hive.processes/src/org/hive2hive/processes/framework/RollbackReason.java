package org.hive2hive.processes.framework;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;

public class RollbackReason {
	
	private static final H2HLogger logger = H2HLoggerFactory.getLogger(RollbackReason.class);
	
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
