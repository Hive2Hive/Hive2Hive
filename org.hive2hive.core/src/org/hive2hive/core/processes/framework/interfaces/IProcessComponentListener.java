package org.hive2hive.core.processes.framework.interfaces;

import org.hive2hive.core.processes.framework.RollbackReason;


public interface IProcessComponentListener {

	void onSucceeded();

	void onFailed(RollbackReason reason);

	void onFinished();
}