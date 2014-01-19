package org.hive2hive.processes.implementations;

import org.hive2hive.processes.framework.RollbackReason;
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
}
