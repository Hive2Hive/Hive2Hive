package org.hive2hive.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTest {

	private final static Logger logger = LoggerFactory.getLogger(LogTest.class);
	
	public static void main(String[] args) {

		logger.trace("Hello world!");
		logger.debug("Hello world!");
		logger.info("Hello world!");
		logger.warn("Hello world!");
		logger.error("Hello world!");
		
		
	}
}
