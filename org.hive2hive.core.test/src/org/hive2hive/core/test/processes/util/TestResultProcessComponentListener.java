package org.hive2hive.core.test.processes.util;

import org.hive2hive.core.processes.framework.interfaces.IProcessResultListener;

public class TestResultProcessComponentListener<T> implements
		IProcessResultListener<T> {

	private boolean resultArrived;
	private T result;
	
	public boolean hasResultArrived() {
		return resultArrived;
	}
	
	public T getResult() {
		return result;
	}
	
	@Override
	public void onResultReady(T result) {
		resultArrived = true;
		this.result = result;
	}
}
