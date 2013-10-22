package org.hive2hive.core.test.network.data;

import org.hive2hive.core.network.data.DataWrapper;

public class TestDataWrapper extends DataWrapper {

	private static final long serialVersionUID = -4190279666159015217L;
	private final String testString;

	public TestDataWrapper(String testContent) {
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
