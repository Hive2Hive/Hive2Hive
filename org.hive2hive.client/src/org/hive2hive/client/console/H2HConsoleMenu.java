package org.hive2hive.client.console;

import org.hive2hive.core.api.interfaces.IH2HNode;

public abstract class H2HConsoleMenu extends ConsoleMenu {

	protected IH2HNode node;
	protected boolean isExpertMode;
	
	public H2HConsoleMenu() {}
	
	public H2HConsoleMenu(IH2HNode node) {
		this.node = node;
	}
	
	public void open(boolean isExpertMode) {
		this.isExpertMode = isExpertMode;
		open();
	}
}
