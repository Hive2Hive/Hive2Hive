package org.hive2hive.client.console;

import org.hive2hive.client.util.MenuContainer;

public abstract class H2HConsoleMenu extends ConsoleMenu {

	protected final MenuContainer menus;
	protected boolean isExpertMode;

	public H2HConsoleMenu(MenuContainer menus) {
		this.menus = menus;
	}

	public void open(boolean isExpertMode) {
		this.isExpertMode = isExpertMode;
		open();
	}

	public void reset() {
		// do nothing by default
	}
}