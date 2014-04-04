package org.hive2hive.client.menu;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItemFactory;
import org.hive2hive.client.menu.basic.BasicMenu;
import org.hive2hive.client.menu.expert.ExpertMenu;

/**
 * The topmost intro menu of the console client.
 * @author Christian
 *
 */
public class IntroMenu extends ConsoleMenu {

	private final NodeMenu nodeMenu = new NodeMenu();
	
	@Override
	protected void addMenuItems() {
		
		add(new H2HConsoleMenuItemFactory("Basic Mode (Recommended)") {
			@Override
			protected void execute() throws Exception {
				new BasicMenu(nodeMenu).open();
			}
		});
		add(new H2HConsoleMenuItemFactory("Expert Mode") {
			@Override
			protected void execute() throws Exception {
				new ExpertMenu(nodeMenu).open();
			}
		});
	}
	
	@Override
	protected void addExitItem() {
		add(new H2HConsoleMenuItemFactory("Exit") {
			protected void execute() {
				exit();
			}
		});
	}

	@Override
	protected void onMenuExit() {
		
		// TODO check whether network indeed has to be shut down here, e.g., when bootstrapped -> just leave
		
		// shutdown network
		nodeMenu.disconnectNode();
		
//		// shutdown file observer
//		if (fileObserverMenu != null && fileObserverMenu.getWatcher() != null) {
//			try {
//				fileObserverMenu.getWatcher().stop();
//			} catch (Exception e) {
//				printError(e.getMessage());
//			}
//		}
	}

	@Override
	protected String getInstruction() {
		return "Do you want to use the console in basic or expert mode?";
	}

}
