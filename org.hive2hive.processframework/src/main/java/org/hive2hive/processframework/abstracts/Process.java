package org.hive2hive.processframework.abstracts;

import java.util.List;

/**
 * Abstract base class for all composite (container) process components that contain other process components.
 * 
 * @author Christian
 * 
 */
public abstract class Process extends ProcessComponent {

	public final void add(ProcessComponent component) {
		component.setParent(this);
		doAdd(component);
	};

	public final void insert(int index, ProcessComponent component) {
		component.setParent(this);
		doInsert(index, component);
	}

	public final void insertNext(ProcessComponent toInsert, ProcessComponent after) {
		List<ProcessComponent> components = getComponents();
		int index = components.size();
		for (int i = 0; i < components.size(); i++) {
			if (components.get(i).equals(after)) {
				index = i + 1;
				break;
			}
		}

		insert(index, toInsert);
	}

	public final void remove(ProcessComponent component) {
		component.setParent(null);
		doRemove(component);
	}

	protected abstract void doAdd(ProcessComponent component);

	protected abstract void doInsert(int index, ProcessComponent component);

	protected abstract void doRemove(ProcessComponent component);

	public abstract List<ProcessComponent> getComponents();

	// public abstract IProcessIterator createIterator();

}
