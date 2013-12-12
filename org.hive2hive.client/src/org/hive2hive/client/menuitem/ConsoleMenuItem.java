package org.hive2hive.client.menuitem;

/**
 * An abstract console menu item representing an option. It predefines the execution of an operation by means
 * of a template method.
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
		} catch (Exception e) {
			System.err.println("An exception has been thrown:");
			e.printStackTrace();
			System.out.println();
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
