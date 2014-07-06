package org.hive2hive.processframework.abstracts;

import org.hive2hive.processframework.interfaces.IProcessComponent;

/**
 * Abstract base class for all process component decorators that provide additional behavior or state to
 * existing components.
 * 
 * @author Christian
 * 
 */
public abstract class ProcessDecorator extends ProcessComponent {

	protected final IProcessComponent decoratedComponent;

	public ProcessDecorator(IProcessComponent decoratedComponent) {
		this.decoratedComponent = decoratedComponent;

	}

}
