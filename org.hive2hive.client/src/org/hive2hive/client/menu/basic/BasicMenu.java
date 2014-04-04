package org.hive2hive.client.menu.basic;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.menu.NodeMenu;

public class BasicMenu extends ConsoleMenu {

	private final NodeMenu nodeCreationMenu;

	public BasicMenu(NodeMenu nodeCreationMenu) {
		this.nodeCreationMenu = nodeCreationMenu;
	}
	
	@Override
	protected void createItems() {
	}
	
	@Override
	protected void addMenuItems() {
		
		add(new H2HConsoleMenuItem("Connect") {
			
			@Override
			protected void execute() throws Exception {
				nodeCreationMenu.open(true);
			}
		});
	}

	@Override
	protected String getInstruction() {
		return "Choose action:";
	}

}