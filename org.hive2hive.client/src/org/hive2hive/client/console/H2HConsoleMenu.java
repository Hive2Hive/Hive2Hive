package org.hive2hive.client.console;

import org.hive2hive.core.api.interfaces.IH2HNode;

public abstract class H2HConsoleMenu extends ConsoleMenu {

	protected IH2HNode node;
	
	public H2HConsoleMenu(IH2HNode node) {
		this.node = node;
	}
	
}
