package org.hive2hive.processes.framework.concretes;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.hive2hive.processes.framework.abstracts.Process;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.abstracts.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public class SequentialProcess extends Process {

	LinkedList<ProcessComponent> components = new LinkedList<ProcessComponent>();
	Iterator<ProcessComponent> iterator = components.iterator();

	private boolean isPaused; // TODO might be moved up

	@Override
	public void join() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {

		while (iterator.hasNext() && !isPaused) {
			iterator.next().start();
		}
	}

	@Override
	protected void doPause() {
		isPaused = true;
	}
	
	@Override
	protected void doResumeExecution() throws InvalidProcessStateException {
		isPaused = false;
		doExecute();
	}
	
	@Override
	protected void doResumeRollback() {
		isPaused = false;
		doRollback(new RollbackReason(this, "Rollback resumed."));
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
