package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.processes.framework.concretes.ProcessComponentListener;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponentListener;

public class TestProcessComponentListener extends ProcessComponentListener implements IProcessComponentListener {

	public void reset() {
		hasSucceeded = false;
		hasFailed = false;
		hasFinished = false;
	}

}