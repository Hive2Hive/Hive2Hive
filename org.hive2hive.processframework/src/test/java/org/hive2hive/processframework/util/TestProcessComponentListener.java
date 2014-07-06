package org.hive2hive.processframework.util;

import org.hive2hive.processframework.concretes.ProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;

public class TestProcessComponentListener extends ProcessComponentListener implements IProcessComponentListener {

	public void reset() {
		hasSucceeded = false;
		hasFailed = false;
		hasFinished = false;
	}

}