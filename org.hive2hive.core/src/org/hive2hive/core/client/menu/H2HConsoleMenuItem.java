package org.hive2hive.core.client.menu;

public abstract class H2HConsoleMenuItem extends ConsoleMenuItem {

	boolean hasException = false;
	Exception catchedException = null;
	
	public H2HConsoleMenuItem(String displayText) {
		super(displayText);
	}

	@Override
	public void invoke() {
		
		hasException = false;
		catchedException = null;
		
		initialize();
		try {
			execute();
		} catch (Exception e){
			hasException = true;
			catchedException = e;
		} finally {
			end();
		}
	}
	
	@Override
	protected void initialize() {
		printSelection();
	}

	@Override
	protected abstract void execute();

	@Override
	protected void end() {
		if (hasException){
			System.err.println(String.format("An exception has been thrown:\n %s\n", catchedException.getMessage()));
		}
	}

	private void printSelection() {
		System.out.println(String.format("Selected Option: %s", displayText));
	}
}
