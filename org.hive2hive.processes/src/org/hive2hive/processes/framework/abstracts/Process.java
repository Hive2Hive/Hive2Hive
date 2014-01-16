package org.hive2hive.processes.framework.abstracts;

import java.util.Collection;

public abstract class Process extends ProcessComponent {

	private Collection<ProcessComponent> components;
	
	public void add(ProcessComponent component){
		
	}
	
	public void remove(ProcessComponent component){
		
	}
	
	public Collection<ProcessComponent> getComponents() {
		return components;
	}
	
	@Override
	public void join() {
		// TODO Auto-generated method stub

	}

}
