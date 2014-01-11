package org.hive2hive.core.process.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessManager;
import org.hive2hive.core.process.listener.IProcessListener;

/**
 * Helps building process trees. A child process starts when the parent process has successfully finished
 * 
 * @author Nico
 * 
 */
// TODO the listeners in the root process and the progress does not work here
public abstract class ProcessTreeNode extends Process {

	private final Process process;
	private final List<ProcessTreeNode> childProcesses;
	private final ProcessTreeNode parent;
	private final List<Exception> exceptionList;
	private final AtomicBoolean done;
	private final AtomicBoolean childrenStarted;

	/**
	 * For the root node (does not do anything except holding children and starting them simultaneously
	 */
	public ProcessTreeNode() {
		this(null, null);
	}

	/**
	 * For process nodes that hold process. The process is not started until the parent has finished
	 * 
	 * @param process
	 * @param parent
	 * @param node
	 */
	public ProcessTreeNode(Process process, ProcessTreeNode parent) {
		super(null);
		this.process = process;
		this.parent = parent;
		this.childProcesses = new ArrayList<ProcessTreeNode>();
		done = new AtomicBoolean(false);
		childrenStarted = new AtomicBoolean(false);

		if (parent == null) {
			// root node
			exceptionList = new CopyOnWriteArrayList<Exception>();
		} else {
			// child node
			exceptionList = null;
			parent.addChild(this);
		}
	}

	public void addChild(ProcessTreeNode childProcess) {
		childProcesses.add(childProcess);
	}

	public List<ProcessTreeNode> getChildren() {
		return childProcesses;
	}

	public List<ProcessTreeNode> getAllChildren() {
		List<ProcessTreeNode> allChildren = new ArrayList<ProcessTreeNode>();

		for (ProcessTreeNode child : getChildren()) {
			allChildren.add(child);
			allChildren.addAll(child.getAllChildren());
		}
		return allChildren;
	}

	public ProcessTreeNode getParent() {
		return parent;
	}

	/**
	 * Returns whether all children are done as well
	 * 
	 * @return
	 */
	public boolean isDone() {
		// when failed, the children must not be checked since they never started
		if (childrenStarted.get())
			return done.get();

		// children may have started and are already finished (note the recursion)
		boolean allChildrenDone = true;
		for (ProcessTreeNode child : childProcesses) {
			allChildrenDone &= child.isDone();
		}

		return done.get() && allChildrenDone;
	}

	public int getDepth() {
		if (parent == null) {
			return 0;
		} else {
			return parent.getDepth() + 1;
		}
	}

	protected void addProblem(Exception reason) {
		if (parent == null) {
			exceptionList.add(reason);
		} else {
			parent.addProblem(reason);
		}
	}

	@Override
	public void run() {
		if (parent == null) {
			setNextStep(null);

			// is root node --> start all children
			startChildren();

			// no further tasks for the root node
			done.set(true);
		} else {
			// after the current process is done, start the next process
			process.addListener(new IProcessListener() {
				@Override
				public void onSuccess() {
					ProcessManager.getInstance().detachProcess(ProcessTreeNode.this);
					done.set(true);
					startChildren();
				}

				@Override
				public void onFail(Exception exception) {
					addProblem(exception);
					ProcessManager.getInstance().detachProcess(ProcessTreeNode.this);
					done.set(true);
				}
			});

			process.start();
		}
	}

	private void startChildren() {
		for (ProcessTreeNode child : childProcesses) {
			child.start();
		}

		childrenStarted.set(true);
	}

	public List<Exception> getExceptionList() {
		if (parent == null) {
			return exceptionList;
		} else {
			return parent.getExceptionList();
		}
	}
}
