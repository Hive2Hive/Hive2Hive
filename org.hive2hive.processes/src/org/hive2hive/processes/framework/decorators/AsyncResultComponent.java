package org.hive2hive.processes.framework.decorators;

import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.interfaces.IProcessResultListener;
import org.hive2hive.processes.framework.interfaces.IResultProcessComponent;

public class AsyncResultComponent<T> extends AsyncComponent implements IResultProcessComponent<T> {

	public AsyncResultComponent(ProcessComponent decoratedComponent) {
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
}
