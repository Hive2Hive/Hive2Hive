package org.hive2hive.client.menu;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;

/**
 * The topmost intro menu of the console client.
 * @author Christian
 *
 */
public class IntroMenu extends ConsoleMenu {

	private final NodeMenu nodeMenu = new NodeMenu();
	private final UserMenu userMenu = new UserMenu();
	private final FileMenu fileMenu = new FileMenu(nodeMenu);
	
	@Override
	protected void addMenuItems() {
		
		add(new H2HConsoleMenuItem("Basic Mode (Recommended)") {
			@Override
			protected void execute() throws Exception {
				new RootMenu(nodeMenu, userMenu, fileMenu).open(false);
			}
		});
		add(new H2HConsoleMenuItem("Expert Mode") {
			@Override
			protected void execute() throws Exception {
				new RootMenu(nodeMenu, userMenu, fileMenu).open(true);
			}
		});
	}
	
	@Override
	protected String getExitItemText() {
		return "Exit";
	}

	@Override
	protected void onMenuExit() {
		
		// TODO check whether network indeed has to be shut down here, e.g., when bootstrapped -> just leave
		
		// shutdown network
		System.out.println("Disconnecting from the network...");
		nodeMenu.disconnectNode();
		
		// TODO stop file observer
		
	
	}

	@Override
	protected String getInstruction() {
		return "Do you want to use the console in basic or expert mode?";
	}

}
