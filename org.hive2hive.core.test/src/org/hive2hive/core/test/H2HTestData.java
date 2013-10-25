package org.hive2hive.core.test;

import org.hive2hive.core.network.data.NetworkData;

public class H2HTestData extends NetworkData {

	private static final long serialVersionUID = -4190279666159015217L;
	private final String testString;

	public H2HTestData(String testContent) {
		this.testString = testContent;
	}

	@Override
	public int getTimeToLive() {
		return 10000;
	}

	public String getTestString() {
		return testString;
	}

}
