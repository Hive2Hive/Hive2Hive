package org.hive2hive.core.client.menu;

public abstract class H2HConsoleMenuItem extends ConsoleMenuItem {

	public H2HConsoleMenuItem(String displayText) {
		super(displayText);
	}

	@Override
	protected final void initialize() {
		printSelection();
//		printInstruction();
	}

	@Override
	protected abstract void execute();

	@Override
	protected final void end() {
		// TODO Auto-generated method stub

	}

	private void printSelection() {
		System.out.println(String.format("Selected Option: %s", displayText));
	}
	
	private void printInstruction() {
		System.out.println(String.format("Selected Option: %s", displayText));
	}
}
