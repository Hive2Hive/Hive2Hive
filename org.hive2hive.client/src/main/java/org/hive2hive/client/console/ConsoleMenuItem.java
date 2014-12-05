package org.hive2hive.client.console;

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

		boolean satisfied = false;
		try {
			satisfied = checkPreconditions();
		} catch (Exception e) {
			ConsoleMenu.printError("Exception during precondition check.");
			ConsoleMenu.printError(e);
		}

		if (satisfied) {
			initialize();
			try {
				execute();
			} catch (Exception e) {
				ConsoleMenu.printError("Exception during menu item execution.");
				ConsoleMenu.printError(e);
			} finally {
				end();
			}
		}
	}

	protected boolean checkPreconditions() throws Exception {
		// nothing by default
		return true;
	}

	protected abstract void initialize();

	protected abstract void execute() throws Exception;

	protected abstract void end();

	public String getDisplayText() {
		return displayText;
	}

}
