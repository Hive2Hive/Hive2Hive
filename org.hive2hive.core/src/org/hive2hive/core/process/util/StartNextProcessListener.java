package org.hive2hive.core.process.util;

import java.util.List;

import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.listener.IProcessListener;

public class StartNextProcessListener implements IProcessListener {

	private final Process nextProcess;
	private final Process currentProcess;
	private final boolean startAtFail;

	/**
	 * Listener that starts the next process as soon the current process has been finished
	 * 
	 * @param nextProcess
	 * @param currentProcess
	 * @param startAtFail
	 */
	public StartNextProcessListener(Process nextProcess, Process currentProcess, boolean startAtFail) {
		this.nextProcess = nextProcess;
		this.currentProcess = currentProcess;
		this.startAtFail = startAtFail;
	}

	@Override
	public void onSuccess() {
		if (nextProcess != null) {
			// next process exists, continue with it
			moveListenersAndStart();
		}
	}

	@Override
	public void onFail(Exception exception) {
		if (startAtFail)
			moveListenersAndStart();
	}

	/**
	 * Copy the listeners when the process has already finished because the user could attach more listeners
	 * to it
	 */
	private void moveListenersAndStart() {
		// move the listeners from the first process until the last process
		List<IProcessListener> listeners = currentProcess.getListeners();
		for (IProcessListener toCopy : listeners) {
			if (!(toCopy instanceof StartNextProcessListener))
				nextProcess.addListener(toCopy);
		}

		for (IProcessListener toRemove : listeners) {
			currentProcess.removeListener(toRemove);
		}

		nextProcess.start();
	}
}
