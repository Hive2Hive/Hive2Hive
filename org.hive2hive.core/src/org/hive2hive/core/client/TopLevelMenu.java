package org.hive2hive.core.client;

public class TopLevelMenu extends ConsoleMenu {

	public TopLevelMenu(H2HConsole console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void addMenuHandlers() {
		
		add("User Configuration", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("User Configuration");
				userConfigurationHandler();
			}
		});
		add("Network Configuration", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Network Configuration");
				networkConfigurationHandler();
			}
		});
		add("Register", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Register");
				registerHandler();
			}
		});
		add("Login", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Login");
				loginHandler();
			}
		});

	}
	
	private void userConfigurationHandler() {
		
	}
	
	private void networkConfigurationHandler() {
		
		NetworkMenu menu = new NetworkMenu(console, session);
		menu.open();
	}

	private void registerHandler() {

		RegisterMenu menu = new RegisterMenu(console, session);
		menu.open();
	}

	private void loginHandler() {
		
		
	}
	
	@Override
	public String getInstruction() {
		return "Please select an option.";
	}
}
