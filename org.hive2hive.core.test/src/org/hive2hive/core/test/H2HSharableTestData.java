package org.hive2hive.core.test;

import org.hive2hive.core.network.data.SharableNetworkContent;

public class H2HSharableTestData extends SharableNetworkContent {

	private static final long serialVersionUID = -1848976941947242945L;
	private final String testString;

	public H2HSharableTestData(String testContent) {
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
