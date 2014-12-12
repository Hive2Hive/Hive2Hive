package org.hive2hive.client.console;

import org.hive2hive.client.util.MenuContainer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class H2HConsoleMenu extends ConsoleMenu {

	protected final MenuContainer menus;
	protected final Config config;
	protected boolean isExpertMode;

	public H2HConsoleMenu(MenuContainer menus) {
		this.menus = menus;
		this.config = ConfigFactory.load("client.conf");
	}

	public void open(boolean isExpertMode) {
		this.isExpertMode = isExpertMode;
		open();
	}

	public void reset() {
		// do nothing by default
	}
}