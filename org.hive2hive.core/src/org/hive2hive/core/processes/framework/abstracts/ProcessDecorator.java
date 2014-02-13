package org.hive2hive.core.processes.framework.abstracts;

import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

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
