package org.hive2hive.client.menu;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.util.FileObserver;
import org.hive2hive.client.util.MenuContainer;
import org.hive2hive.core.api.interfaces.IH2HNode;

/**
 * The topmost intro menu of the console client.
 * 
 * @author Christian
 *
 */
public class IntroMenu extends ConsoleMenu {

	private final MenuContainer menus;

	public IntroMenu() {
		menus = new MenuContainer();
	}

	@Override
	protected void addMenuItems() {

		add(new H2HConsoleMenuItem("Basic Mode (Recommended)") {
			@Override
			protected void execute() throws Exception {
				menus.setExpertMode(false);
				menus.getRootMenu().open();
			}
		});
		add(new H2HConsoleMenuItem("Expert Mode") {
			@Override
			protected void execute() throws Exception {
				menus.setExpertMode(true);
				menus.getRootMenu().open();
			}
		});
	}

	@Override
	protected String getExitItemText() {
		return "Exit";
	}

	@Override
	protected void onMenuExit() {
		shutdown();
	}

	public void shutdown() {
		// TODO check whether network indeed has to be shut down here, e.g., when bootstrapped -> just leave

		// shutdown network
		IH2HNode node = menus.getNodeMenu().getNode();
		if (node != null && node.isConnected()) {
			print("Disconnecting from the network...");
			node.disconnect();
		}

		// stop file observer
		FileObserver fileObserver = menus.getFileObserverMenu().getFileObserver();
		if (fileObserver != null && fileObserver.isRunning()) {
			print("Stopping the file observer...");
			try {
				fileObserver.stop();
			} catch (Exception e) {
				printError(e);
			}
		}
	}

	@Override
	protected String getInstruction() {
		return "Do you want to use the console in basic or expert mode?";
	}

}
