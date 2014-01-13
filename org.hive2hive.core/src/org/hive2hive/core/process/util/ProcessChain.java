package org.hive2hive.core.process.util;

import java.util.List;

import org.hive2hive.core.exceptions.IllegalProcessStateException;
import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.process.Process;
import org.hive2hive.core.process.ProcessState;

public class ProcessChain extends Process {

	private static final H2HLogger logger = H2HLoggerFactory.getLogger(ProcessChain.class);

	private final boolean startAtFail;
	private final List<Process> processes;

	public ProcessChain(List<Process> processes, boolean startAtFail) {
		super(null);
		this.processes = processes;
		this.startAtFail = startAtFail;
	}

	@Override
	public void run() {
		// execute all processes
		for (Process process : processes) {
			try {
				process.start();
				process.join();
			} catch (IllegalProcessStateException | InterruptedException e1) {
				logger.error("Could not wait until process " + process.getClass().getSimpleName()
						+ " has finished");
			}

			if (process.getState() == ProcessState.FINISHED) {
				// everything is ok, continue with next process in chain
				continue;
			} else {
				// process somehow stopped, rollbacked or could not wait
				if (startAtFail) {
					continue;
				} else {
					break;
				}
			}
		}

		// finalize
		terminate();
	}
}
