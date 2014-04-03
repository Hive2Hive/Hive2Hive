package org.hive2hive.client.console;

import org.hive2hive.client.util.Formatter;

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
		Formatter.setSuccessForeground();
		System.out.println(String.format("Selected Option: %s", displayText));
		Formatter.setDefaultForeground();
	}

	protected void printPreconditionError(String message) {
		Formatter.setErrorForeground();
		System.out.println(String.format("Unsatisfied Precondition: %s", message));
		Formatter.setDefaultForeground();
	}

	private void printExecuted() {
		Formatter.setSuccessForeground();
		System.out.println(String.format("%s executed.", displayText));
		Formatter.setDefaultForeground();
	}
}
