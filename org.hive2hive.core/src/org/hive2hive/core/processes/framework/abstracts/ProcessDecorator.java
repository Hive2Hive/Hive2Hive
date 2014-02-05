package org.hive2hive.core.processes.framework.abstracts;


public abstract class ProcessDecorator extends ProcessComponent {

	protected final ProcessComponent decoratedComponent;

	public ProcessDecorator(ProcessComponent decoratedComponent) {
		this.decoratedComponent = decoratedComponent;

	}

}
