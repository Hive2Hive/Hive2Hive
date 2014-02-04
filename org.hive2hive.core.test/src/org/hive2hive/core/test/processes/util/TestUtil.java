package org.hive2hive.core.test.processes.util;


public final class TestUtil {

	public static void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
