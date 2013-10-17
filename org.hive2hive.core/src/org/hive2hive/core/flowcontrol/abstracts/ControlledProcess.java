package org.hive2hive.core.flowcontrol.abstracts;

import org.hive2hive.core.flowcontrol.interfaces.IProcess;
import org.hive2hive.core.flowcontrol.manager.ProcessController;

/** This abstract class is the very basis of every process that gets controlled by the ProcessController.
 * 
 * @author Christian
 *
 */
public abstract class ControlledProcess implements IProcess {
	
	private int id;
	
	@Override
	public final void run() {
		
		createID();
		register();
		process();
		unregister();
	}
	
	private final void createID() {
		id = 0;
	}
	
	private final void register() {
		ProcessController.getInstance().attachProcess(this);
	}
	
	private final void unregister() {
		ProcessController.getInstance().detachProcess(this);
	}

	public final int getID() {
		return id;
	}

}
