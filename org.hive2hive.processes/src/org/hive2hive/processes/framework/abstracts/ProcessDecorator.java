package org.hive2hive.processes.framework.abstracts;

import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;

public abstract class ProcessDecorator extends ProcessComponent {

	protected final ProcessComponent decoratedComponent;

	public ProcessDecorator(ProcessComponent decoratedComponent) {
		this.decoratedComponent = decoratedComponent;
	}

	@Override
	public void attachListener(IProcessComponentListener listener) {
		decoratedComponent.attachListener(listener);

		// Note: in case a decorator wants to attach listeners, it can do so by overriding this method. But
		// then, the current usage (whether attached to decorator or component directly) must be checked (and
		// corrected).
	}

	@Override
	public void detachListener(IProcessComponentListener listener) {
		decoratedComponent.attachListener(listener);
	}

}
