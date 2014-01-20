package org.hive2hive.processes.framework.concretes;

import java.util.Collection;
import java.util.LinkedList;

import org.hive2hive.processes.framework.ProcessState;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.abstracts.Process;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class SequentialProcess extends Process {

	LinkedList<ProcessComponent> components = new LinkedList<ProcessComponent>();

	private int executionIndex = 0;
	private int rollbackIndex = 0;

	@Override
	public void join() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
				
		while (executionIndex < components.size() && getState() == ProcessState.RUNNING) {
			
			ProcessComponent next = components.get(executionIndex);
			next.start();
			executionIndex++;
		}
		
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
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {

		// reason component rolls itself back before notifying parent component
		rollbackIndex = executionIndex; 
		
		while (rollbackIndex >= 0 && getState() == ProcessState.ROLLBACKING) {
			
			ProcessComponent last = components.get(rollbackIndex);
			last.cancel(reason);
			rollbackIndex--;
		}

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
