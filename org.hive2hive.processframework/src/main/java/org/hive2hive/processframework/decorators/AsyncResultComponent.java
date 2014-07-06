package org.hive2hive.processframework.decorators;

import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessResultListener;
import org.hive2hive.processframework.interfaces.IResultProcessComponent;

/**
 * Decorator for asynchronous components that intend to return a result.
 * 
 * @author Christian
 * 
 * @param <T>
 */
public class AsyncResultComponent<T> extends AsyncComponent implements IResultProcessComponent<T> {

	public AsyncResultComponent(IProcessComponent decoratedComponent) {
		super(decoratedComponent);

		// TODO Chris: find a cleaner way --> update whole framework hierarchy with IResultProcessComponent<T>
		if (!(decoratedComponent instanceof IResultProcessComponent<?>)) {
			throw new IllegalArgumentException(
					"Cannot decorate this component as it does not implement IResultProcessComponent<T>.");
		}
	}

	@Override
	public void attachListener(IProcessResultListener<T> listener) {

		((IResultProcessComponent<T>) decoratedComponent).attachListener(listener);
	}

	@Override
	public void detachListener(IProcessResultListener<T> listener) {
		((IResultProcessComponent<T>) decoratedComponent).detachListener(listener);
	}

	@Override
	public void notifyResultComputed(T result) {
		((IResultProcessComponent<T>) decoratedComponent).notifyResultComputed(result);
	}

	@Override
	public T getResult() {
		return ((IResultProcessComponent<T>) decoratedComponent).getResult();
	}
}
