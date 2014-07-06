package org.hive2hive.processframework.concretes;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.interfaces.IProcessResultListener;
import org.hive2hive.processframework.interfaces.IResultProcessComponent;

/**
 * A process step that intends to return a result.
 * 
 * @author Christian
 * 
 * @param <T> The type of the result object.
 */
public abstract class ResultProcessStep<T> extends ProcessStep implements IResultProcessComponent<T> {

	private final List<IProcessResultListener<T>> listener = new ArrayList<IProcessResultListener<T>>();

	@Override
	public void notifyResultComputed(T result) {
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
