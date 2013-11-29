package org.hive2hive.core.client.menu;

import org.hive2hive.core.client.H2HConsole;

/**
 * A console menu item representing an option.
 * 
 * @author Christian
 * 
 */
public abstract class ConsoleMenuItem {

	protected String displayText;

	protected abstract void initialize();
	protected abstract void execute() throws Exception;
	protected abstract void end();
		
	public ConsoleMenuItem(String displayText) {
		this.displayText = displayText;
	}
	
	public final void invoke() {
		
		initialize();
		try {
			execute();
		} catch (Exception e){
			System.err.println(String.format("An exception has been thrown:\n%s\n", e.getMessage()));
		} finally {
			end();
		}
	}

	public String getDisplayText() {
		return displayText;
	}
}
