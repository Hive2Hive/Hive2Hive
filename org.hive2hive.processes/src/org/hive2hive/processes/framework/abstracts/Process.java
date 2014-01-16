package org.hive2hive.processes.framework.abstracts;

public abstract class Process extends ProcessComponent {

	public abstract void add(ProcessComponent component);

	public abstract void remove(ProcessComponent component);
	
//	public abstract IProcessIterator createIterator();

	@Override
	public abstract void join();

}
