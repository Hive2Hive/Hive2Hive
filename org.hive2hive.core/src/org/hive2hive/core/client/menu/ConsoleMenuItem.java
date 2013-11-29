package org.hive2hive.core.client.menu;

/**
 * A console menu item representing an option.
 * 
 * @author Christian
 * 
 */
public abstract class ConsoleMenuItem {

	protected String displayText;
//	private IConsoleMenuCallback callback;

	protected abstract void initialize();
	protected abstract void execute();
	protected abstract void end();
	
	public ConsoleMenuItem(String displayText) {
		this.displayText = displayText;
//		this.callback = callback;
	}
	
	public final void invoke() {
		initialize();
		execute();
		end();
	}

	public String getDisplayText() {
		return displayText;
	}

//	public IConsoleMenuCallback getCallback() {
//		return callback;
//	}
}
