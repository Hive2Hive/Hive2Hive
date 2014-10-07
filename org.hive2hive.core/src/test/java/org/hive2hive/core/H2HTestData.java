package org.hive2hive.core;

import org.hive2hive.core.model.versioned.BaseVersionedNetworkContent;

public class H2HTestData extends BaseVersionedNetworkContent {

	private static final long serialVersionUID = -4190279666159015217L;
	private String testString;

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

	public void setTestString(String testString) {
		this.testString = testString;
	}

}
