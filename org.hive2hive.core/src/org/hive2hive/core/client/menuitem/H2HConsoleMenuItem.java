package org.hive2hive.core.client.menuitem;

/**
 * A specified console menu item. Allows to check preconditions before execution the operation.
 * 
 * @author Christian
 * 
 */
public abstract class H2HConsoleMenuItem extends ConsoleMenuItem {

	public H2HConsoleMenuItem(String displayText) {
		super(displayText);
	}

	@Override
	public void invoke() {
		// check preconditions before invoking
		checkPreconditions();
		super.invoke();
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

	protected void checkPreconditions() {
		// nothing by default
	}

	private void printSelection() {
		System.out.println(String.format("Selected Option: %s\n", displayText));
	}

	protected void printPreconditionError(String message) {
		System.out.println(String.format("Unsatisfied Precondition: %s\n", message));
	}

	private void printExecuted() {
		System.out.println(String.format("\n%s executed.\n", displayText));
	}
}
