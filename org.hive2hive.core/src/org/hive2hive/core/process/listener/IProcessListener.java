package org.hive2hive.core.process.listener;

public interface IProcessListener {

	void onSuccess();
	
	void onFail(String reason);
	
}
