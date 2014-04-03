package org.hive2hive.client.menu;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;

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
		// TODO Auto-generated method stub

	}

	@Override
	protected String getInstruction() {
		return "Do you want to use the basic or advanced console?";
	}

}
