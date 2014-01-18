package org.hive2hive.processes.framework.abstracts;

import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;


public abstract class ProcessDecorator extends ProcessComponent {

	protected final ProcessComponent decoratedComponent;

	public ProcessDecorator(ProcessComponent decoratedComponent) {
		this.decoratedComponent = decoratedComponent;
	}
	
	@Override
	public void attachListener(IProcessComponentListener listener) {
		super.attachListener(listener);
		decoratedComponent.attachListener(listener);
	}

	@Override
	public void detachListener(IProcessComponentListener listener) {
		super.detachListener(listener);
		decoratedComponent.attachListener(listener);
	}
	
}
