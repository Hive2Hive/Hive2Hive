package org.hive2hive.client.util;

import java.io.IOException;

import org.hive2hive.core.log.H2HLoggerFactory;

public final class LoggerInit {

	public static void initLogger() {
		try {
			H2HLoggerFactory.initFactory();
		} catch (IOException e) {
			System.err.println("H2HLoggerFactory could not be initialized.");
		}
	}
}
