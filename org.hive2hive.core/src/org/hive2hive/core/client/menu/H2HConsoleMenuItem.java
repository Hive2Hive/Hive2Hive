package org.hive2hive.core.client.menu;

public abstract class H2HConsoleMenuItem extends ConsoleMenuItem {

	public H2HConsoleMenuItem(String displayText) {
		super(displayText);
	}

	@Override
	public void invoke() {
		// check preconditions before invoking
		if (preconditionsSatisfied()){
			super.invoke();
		}
	}
	
	@Override
	protected final void initialize() {
		printSelection();
	}

	@Override
	protected abstract void execute() throws Exception;

	@Override
	protected void end() {
		// do nothing by default
	}

	protected boolean preconditionsSatisfied() {
		// accept by default
		return true;
	}
	
	private void printSelection() {
		System.out.println(String.format("Selected Option: %s\n", displayText));
	}

	protected void printSuccess() {
		System.out.println(String.format("%s executed.\n", displayText));
	}
}
