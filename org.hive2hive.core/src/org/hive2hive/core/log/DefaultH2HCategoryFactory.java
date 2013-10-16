package org.hive2hive.core.log;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class DefaultH2HCategoryFactory implements LoggerFactory {

	@Override
	public Logger makeNewLoggerInstance(String name) {
		return new H2HLogger(name);
	}

}
