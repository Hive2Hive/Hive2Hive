package org.hive2hive.core.processes.framework.decorators;

import org.hive2hive.core.processes.framework.RollbackReason;

public interface ICompletionHandle {

	void onCompletionSuccess();
	
	void onCompletionFailure(RollbackReason reason);
}
