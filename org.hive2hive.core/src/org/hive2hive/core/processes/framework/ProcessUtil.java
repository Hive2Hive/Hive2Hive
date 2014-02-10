package org.hive2hive.core.processes.framework;

import java.util.UUID;

/**
 * Util for common operations used by the process framework.
 * 
 * @author Christian
 * 
 */
public final class ProcessUtil {

	public static String generateID() {
		return UUID.randomUUID().toString();
	}
}
