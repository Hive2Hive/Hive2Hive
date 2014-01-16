package org.hive2hive.processes.framework.concretes;

import java.util.LinkedList;

import org.hive2hive.processes.framework.abstracts.Process;
import org.hive2hive.processes.framework.abstracts.ProcessComponent;
import org.hive2hive.processes.framework.abstracts.RollbackReason;

public class SequentialProcess extends Process {

	LinkedList<ProcessComponent> components = new LinkedList<ProcessComponent>();

	@Override
	public void join() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doExecute() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doPause() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doResume() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRollback(RollbackReason reason) {
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
}
