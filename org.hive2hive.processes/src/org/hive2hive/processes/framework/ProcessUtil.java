package org.hive2hive.processes.framework;

import java.util.UUID;

import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public final class ProcessUtil {

	public static void wait(ProcessComponent component) throws InvalidProcessStateException {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			component.cancel(new RollbackReason(component, e.getMessage()));
		}
	}
	
	public static String generateID() {
		return UUID.randomUUID().toString();
	}
}
