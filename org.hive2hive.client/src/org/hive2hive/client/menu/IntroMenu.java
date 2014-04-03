package org.hive2hive.client.menu;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;

public class IntroMenu extends ConsoleMenu {

	@Override
	protected void setup() {
		createItems();
		addMenuItems();

		add(new H2HConsoleMenuItem("Exit") {
			protected void execute() {
				exit();
			}
		});
	}
	
	@Override
	protected void addMenuItems() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getInstruction() {
		// TODO Auto-generated method stub
		return null;
	}

}
