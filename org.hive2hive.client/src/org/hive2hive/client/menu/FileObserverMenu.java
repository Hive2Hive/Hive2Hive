package org.hive2hive.client.menu;

import java.io.File;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.core.api.interfaces.IFileManager;
import org.hive2hive.core.file.watcher.H2HFileListener;
import org.hive2hive.core.file.watcher.H2HFileWatcher;
import org.hive2hive.core.file.watcher.H2HFileWatcher.H2HFileWatcherBuilder;

public class FileObserverMenu extends ConsoleMenu {

	private final H2HFileWatcherBuilder watcherBuilder;
	private H2HFileWatcher watcher;
	private IFileManager fileManager;
	private File rootDirectory;

	public FileObserverMenu(File rootDirectory, IFileManager fileManager) {
		this.rootDirectory = rootDirectory;
		watcherBuilder = new H2HFileWatcherBuilder(rootDirectory);
		this.fileManager = fileManager;
	}

	@Override
	protected void addMenuItems() {
		add(new H2HConsoleMenuItem("Set Interval") {
			protected void execute() {
				System.out.println("Specify the observation interval (ms):");
				watcherBuilder.setInterval(awaitIntParameter());
			}
		});
		add(new H2HConsoleMenuItem("Set File Filter") {
			protected void execute() {
				// TODO implement file filter setting
			}
		});
		add(new H2HConsoleMenuItem("Set Case Sensitivity") {
			protected void execute() {
				// TODO implement case sensitivity setting
			}
		});
		add(new H2HConsoleMenuItem("Start File Observer") {
			protected void execute() throws Exception {
				watcher = watcherBuilder.build();
				watcher.addFileListener(new H2HFileListener(fileManager, rootDirectory));
				watcher.start();
			}
		});
		add(new H2HConsoleMenuItem("Stop File Observer") {
			protected void execute() throws Exception {
				if (watcher != null)
					watcher.stop();
			}
		});
	}

	public H2HFileWatcher getWatcher() {
		return watcher;
	}

	@Override
	protected String getInstruction() {
		return "Please configure and start/stop the file observer:\n";
	}

}
