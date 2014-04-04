package org.hive2hive.client.menu.basic;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.menu.NodeMenu;

public class BasicMenu extends ConsoleMenu {

	private final NodeMenu nodeMenu;

	public BasicMenu(NodeMenu nodeCreationMenu) {
		this.nodeMenu = nodeCreationMenu;
	}
	
	@Override
	protected void createItems() {
	}
	
	@Override
	protected void addMenuItems() {
		
		add(new H2HConsoleMenuItem("Connect") {
			
			@Override
			protected void execute() throws Exception {
				nodeMenu.open(true);
			}
		});
		
		add(new H2HConsoleMenuItem("Login") {
			
			@Override
			protected void checkPreconditions() {
			}
			
			@Override
			protected void execute() throws Exception {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	protected String getInstruction() {
		return "Choose action:";
	}

}