package org.hive2hive.core.test.network.messages;

import org.hive2hive.core.network.messages.IBaseMessageListener;
import org.junit.Assert;

public class TestBaseMessageListener implements IBaseMessageListener {
	@Override
	public void onSuccess() {
	}

	@Override
	public void onFailure() {
		// should not happen
		Assert.fail("Should not failed.");
	}
}