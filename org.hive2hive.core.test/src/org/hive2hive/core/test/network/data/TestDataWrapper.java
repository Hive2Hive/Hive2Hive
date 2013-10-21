package org.hive2hive.core.test.network.data;

import org.hive2hive.core.network.data.BaseDataWrapper;

public class TestDataWrapper extends BaseDataWrapper {

	private static final long serialVersionUID = -4190279666159015217L;

	public TestDataWrapper(Object content) {
		super(content);
	}

	@Override
	public int getTimeToLive() {
		return 10000;
	}

}
