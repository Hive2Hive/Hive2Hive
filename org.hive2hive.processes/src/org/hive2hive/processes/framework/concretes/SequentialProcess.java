package org.hive2hive.processes.framework.concretes;

import java.util.LinkedList;

import org.hive2hive.processes.framework.abstracts.Process;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;

public class SequentialProcess extends Process {

	LinkedList<ProcessComponent> components = new LinkedList<ProcessComponent>();
	
	@Override
	public void doExecute() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doPause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doResume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doRollback() {
		// TODO Auto-generated method stub

	}

	@Override
	public void add(ProcessComponent component) {
		components.add(component);
	}

	@Override
	public void remove(ProcessComponent component) {
		components.remove(component);
	}

	@Override
	public void join() {
		// TODO Auto-generated method stub
		
	}

}
