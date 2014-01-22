package org.hive2hive.processes.framework.abstracts;

import java.util.List;

public abstract class Process extends ProcessComponent {

	public final void add(ProcessComponent component) {
		component.setParent(this);
		doAdd(component);
	};
	
	public final void insert(int index, ProcessComponent component) {
		component.setParent(this);
		doInsert(index, component);
	}
	
	public final void remove(ProcessComponent component) {
		component.setParent(null);
		doRemove(component);
	}
	
	protected abstract void doAdd(ProcessComponent component);
	
	protected abstract void doInsert(int index, ProcessComponent component);

	protected abstract void doRemove(ProcessComponent component);
	
	public abstract List<ProcessComponent> getComponents();
	
//	public abstract IProcessIterator createIterator();

	@Override
	public abstract void join();

}
