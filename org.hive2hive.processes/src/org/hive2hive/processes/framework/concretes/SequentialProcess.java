package org.hive2hive.processes.framework.concretes;

import java.util.Collection;
import java.util.LinkedList;

import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.Process;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class SequentialProcess extends Process {

	LinkedList<ProcessComponent> components = new LinkedList<ProcessComponent>();

	private int executionIndex = 0;

	@Override
	public void join() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
				
		for (int i = executionIndex; i < components.size(); i++, executionIndex++) {
			ProcessComponent next = components.get(i);
			next.start();
		}
		notifySucceeded();
	}

	@Override
	protected void doPause() {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void doResumeRollback() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doRollback(RollbackReason reason) {
		// TODO Auto-generated method stub
		
		
		
	}

	@Override
	protected void doAdd(ProcessComponent component) {
		components.add(component);
	}

	@Override
	protected void doRemove(ProcessComponent component) {
		components.remove(component);
	}

	@Override
	public Collection<ProcessComponent> getComponents() {
		return components;
	}
	
}
