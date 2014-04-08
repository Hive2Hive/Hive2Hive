package org.hive2hive.client.menu;

import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.api.H2HFileObserver;
import org.hive2hive.core.api.H2HFileObserverListener;
import org.hive2hive.core.api.interfaces.IFileObserver;
import org.hive2hive.core.api.interfaces.IFileObserverListener;

public class FileObserverMenu extends H2HConsoleMenu {

	private final NodeMenu nodeMenu;
	private final FileMenu fileMenu;
	
	private IFileObserver fileObserver;
	private long interval = H2HConstants.DEFAULT_FILE_OBSERVER_INTERVAL;
	
	public FileObserverMenu(NodeMenu nodeMenu, FileMenu fileMenu) {
		this.nodeMenu = nodeMenu;
		this.fileMenu = fileMenu;
	}
	
	@Override
	protected void addMenuItems() {

		if (isExpertMode && !fileObserver.isRunning()) {
			add(new H2HConsoleMenuItem("Set Interval") {
				protected void execute() {
					System.out.println("Specify the observation interval (ms):");
					interval = awaitIntParameter();
				}
			});
		}

		if (!fileObserver.isRunning()) {
			add(new H2HConsoleMenuItem("Start File Observer") {
				protected void checkPreconditions() {
					nodeMenu.forceNetwork();
					fileMenu.forceRootDirectory();
				}
				protected void execute() throws Exception {
					
					fileObserver = new H2HFileObserver(fileMenu.getRootDirectory(), interval);
					
					IFileObserverListener listener = new H2HFileObserverListener(nodeMenu.getNode().getFileManager());
					
					fileObserver.addFileObserverListener(listener);
					
					fileObserver.start();
				}
			});
		} else {
			add(new H2HConsoleMenuItem("Stop File Observer") {
				protected void execute() throws Exception {
					if (fileObserver != null)
						fileObserver.stop();
				}
			});
		}
	}

	@Override
	protected String getInstruction() {

		if (isExpertMode)
			return "Configure and start/stop the file observer:";
		else
			return "Start/stop the file observer:";
	}

}
