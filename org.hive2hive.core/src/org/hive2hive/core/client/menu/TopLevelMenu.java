package org.hive2hive.core.client.menu;

import org.hive2hive.core.client.H2HConsole;
import org.hive2hive.core.client.SessionInstance;
import org.hive2hive.core.process.IProcess;

public class TopLevelMenu extends ConsoleMenu {

	public TopLevelMenu(H2HConsole console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void addMenuHandlers() {
		
		add("Network Configuration", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Network Configuration");
				networkConfigurationHandler();
			}
		});
		add("User Configuration", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("User Configuration");
				userConfigurationHandler();
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
	
	private void networkConfigurationHandler() {
		
		NetworkMenu menu = new NetworkMenu(console, session);
		menu.open();
	}
	
	private void userConfigurationHandler() {
	
		UserMenu menu = new UserMenu(console, session);
		menu.open();
	}

	private void registerHandler() {

		IProcess registerProcess = session.getH2HNode().register(session.getCredentials());
	}

	private void loginHandler() {
		
		
	}
	
	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
