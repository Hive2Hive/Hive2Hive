package org.hive2hive.core.log;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class H2HLogHierarchy extends Hierarchy {

	private static final LoggerFactory defaultFactory = new DefaultH2HCategoryFactory();
	
	public H2HLogHierarchy(Logger root) {
		super(root);
	}

	@Override
	public H2HLogger getLogger(String name) {
		return (H2HLogger)super.getLogger(name, defaultFactory);
	}
}
