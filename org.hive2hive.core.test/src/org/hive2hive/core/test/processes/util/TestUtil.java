package org.hive2hive.core.test.processes.util;


public final class TestUtil {

	public static final int DEFAULT_WAITING_TIME = 1000;
	
	public static void waitDefault() {
		wait(DEFAULT_WAITING_TIME);
	}
	
	public static void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
