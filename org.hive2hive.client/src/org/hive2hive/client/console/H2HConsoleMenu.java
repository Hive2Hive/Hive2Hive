package org.hive2hive.client.console;


public abstract class H2HConsoleMenu extends ConsoleMenu {

	protected boolean isExpertMode;
	
	public H2HConsoleMenu() {}
	
	public void open(boolean isExpertMode) {
		this.isExpertMode = isExpertMode;
		open();
	}
}
