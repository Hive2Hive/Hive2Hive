package org.hive2hive.core.client.menuitem;


/**
 * A console menu item representing an option.
 * 
 * @author Christian
 * 
 */
public abstract class ConsoleMenuItem {

	protected String displayText;

		
	public ConsoleMenuItem(String displayText) {
		this.displayText = displayText;
	}
	
	public void invoke() {
		
		initialize();
		try {
			execute();
		} catch (Exception e){
			System.err.println(String.format("An exception has been thrown:\n%s\n", e.getMessage()));
		} finally {
			end();
		}
	}

	protected abstract void initialize();
	
	protected abstract void execute() throws Exception;

	protected abstract void end();

	public String getDisplayText() {
		return displayText;
	}
}
