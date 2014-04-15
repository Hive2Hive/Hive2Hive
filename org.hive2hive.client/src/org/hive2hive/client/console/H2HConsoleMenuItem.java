package org.hive2hive.client.console;

/**
 * A specified console menu item. Allows to check preconditions before executing the operation.
 * 
 * @author Christian
 * 
 */
public abstract class H2HConsoleMenuItem extends ConsoleMenuItem {

	public H2HConsoleMenuItem(String displayText) {
		super(displayText);
	}

	@Override
	protected final void initialize() {
		printSelection();
	}

	@Override
	protected abstract void execute() throws Exception;

	@Override
	protected final void end() {
		printExecuted();
	}

	private void printSelection() {
		ConsoleMenu.print(String.format("Selected Option: %s", displayText));
		ConsoleMenu.print();
	}

	public static void printPreconditionError(String message) {
		ConsoleMenu.print(String.format("Unsatisfied Precondition: %s", message));
	}
	
	private void printExecuted() {
//		ConsoleMenu.print(String.format("%s executed.", displayText));
		ConsoleMenu.print();
	}
}
