package org.hive2hive.core.processes.framework.abstracts;

/**
 * Abstract base class for all process component decorators that provide additional behavior or state to
 * existing components.
 * 
 * @author Christian
 * 
 */
public abstract class ProcessDecorator extends ProcessComponent {

	protected final ProcessComponent decoratedComponent;

	public ProcessDecorator(ProcessComponent decoratedComponent) {
		this.decoratedComponent = decoratedComponent;

	}

}
