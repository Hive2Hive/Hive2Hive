package org.hive2hive.core.client.menu;

/**
 * A console menu item representing an option.
 * 
 * @author Christian
 * 
 */
public class ConsoleMenuItem {

	private String displayText;
	private IConsoleMenuCallback callback;

	public ConsoleMenuItem(String displayText, IConsoleMenuCallback callback) {
		this.displayText = displayText;
		this.callback = callback;
	}

	public String getDisplayText() {
		return displayText;
	}

	public IConsoleMenuCallback getCallback() {
		return callback;
	}
}
