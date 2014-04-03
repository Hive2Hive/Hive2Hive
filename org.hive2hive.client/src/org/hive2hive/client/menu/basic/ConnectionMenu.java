package org.hive2hive.client.menu.basic;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;

public class ConnectionMenu extends ConsoleMenu {

	@Override
	protected void addMenuItems() {
		add(new H2HConsoleMenuItem("New Newtork") {
			@Override
			protected void execute() throws Exception {
				notImplemented(); // TODO
			}
		});
		add(new H2HConsoleMenuItem("Existing Network") {
			@Override
			protected void execute() throws Exception {
				notImplemented(); // TODO
			}
		});
	}

	@Override
	protected String getInstruction() {
		return "Do you want to create a new network or connect to an existing one?";
	}

}
