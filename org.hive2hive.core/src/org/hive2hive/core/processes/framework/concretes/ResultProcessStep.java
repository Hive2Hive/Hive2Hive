package org.hive2hive.core.processes.framework.concretes;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.processes.framework.abstracts.ProcessStep;
import org.hive2hive.core.processes.framework.interfaces.IProcessResultListener;
import org.hive2hive.core.processes.framework.interfaces.IResultProcessComponent;

public abstract class ResultProcessStep<T> extends ProcessStep implements IResultProcessComponent<T> {

	private final List<IProcessResultListener<T>> listener = new ArrayList<IProcessResultListener<T>>();
	
	protected void notifyResultComputed(T result) {
		for (IProcessResultListener<T> listener : this.listener) {
			listener.onResultReady(result);
		}		
	}

	@Override
	public void attachListener(IProcessResultListener<T> listener) {
		this.listener.add(listener);
	}

	@Override
	public void detachListener(IProcessResultListener<T> listener) {
		this.listener.remove(listener);
	}

}
