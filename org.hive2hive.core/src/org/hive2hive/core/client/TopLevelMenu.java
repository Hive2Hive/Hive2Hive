package org.hive2hive.core.client;

public class TopLevelMenu extends ConsoleMenu {

	public TopLevelMenu(H2HConsole console) {
		super(console);
	}

	@Override
	protected void addMenuHandlers() {
		
		add("Register", new IConsoleMenuCallback() {
			public void invoke() {
				System.out.println("Selected Option: Register");
				registerHandler();
			}
		});
		add("Login", new IConsoleMenuCallback() {
			public void invoke() {
				System.out.println("Selected Option: Login");
			}
		});

	}

	private void registerHandler() {

		RegisterMenu menu = new RegisterMenu(console);
		menu.open();
	}

	@Override
	public String getInstruction() {
		return "Please select an option.";
	}
}
