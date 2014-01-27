package org.hive2hive.processes.test.util;

import org.hive2hive.processes.framework.concretes.ProcessComponentListener;
import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;

public class TestProcessComponentListener extends ProcessComponentListener implements IProcessComponentListener {

	public void reset() {
		hasSucceeded = false;
		hasFailed = false;
		hasFinished = false;
	}

}