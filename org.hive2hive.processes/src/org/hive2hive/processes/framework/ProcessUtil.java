package org.hive2hive.processes.framework;

import java.util.UUID;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public final class ProcessUtil {

	private final static H2HLogger logger = H2HLoggerFactory.getLogger(ProcessUtil.class);

	public static void wait(ProcessComponent component) throws InvalidProcessStateException {
		wait(component, 500);
	}

	public static void wait(ProcessComponent component, int ms) throws InvalidProcessStateException {
		try {
			logger.warn(String.format("Thread '%s' sleeping.", Thread.currentThread().getName()));
			Thread.sleep(ms);
			logger.warn(String.format("Thread '%s' awaken.", Thread.currentThread().getName()));
		} catch (InterruptedException e) {
			component.cancel(new RollbackReason(component, e.getMessage()));
		}
	}

	public static String generateID() {
		return UUID.randomUUID().toString();
	}
}
