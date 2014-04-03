package org.hive2hive.client.menu;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.menu.basic.BasicMenu;
import org.hive2hive.client.menu.expert.ExpertMenu;

/**
 * The topmost intro menu of the console client.
 * @author Christian
 *
 */
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
		
		add(new H2HConsoleMenuItem("Basic Mode (Recommended)") {
			@Override
			protected void execute() throws Exception {
				new BasicMenu().open();
			}
		});
		add(new H2HConsoleMenuItem("Expert Mode") {
			@Override
			protected void execute() throws Exception {
				new ExpertMenu().open();
			}
		});
	}
	
	@Override
	protected void onMenuExit() {
		
		// TODO check whether network indeed has to be shut down here, e.g., when bootstrapped -> just leave
		// TODO check whether it is possible to remotely kill a network
		
//		// shutdown network
//		if (nodeMenu.getH2HNode() != null) {
//			nodeMenu.getH2HNode().disconnect();
//		}
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
