package org.hive2hive.client.menu.basic;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.menu.NodeCreationMenu;

public class BasicMenu extends ConsoleMenu {

	@Override
	protected void createItems() {
	}
	
	@Override
	protected void addMenuItems() {
		
		add(new H2HConsoleMenuItem("Connect") {
			
			@Override
			protected void execute() throws Exception {
				new NodeCreationMenu(false).open();
			}
		});
	}

	@Override
	protected String getInstruction() {
		return "Choose action:";
	}

}