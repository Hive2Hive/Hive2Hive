package org.hive2hive.processframework.decorators;

import org.hive2hive.processframework.RollbackReason;

public interface ICompletionHandle {

	void onCompletionSuccess();
	
	void onCompletionFailure(RollbackReason reason);
}
