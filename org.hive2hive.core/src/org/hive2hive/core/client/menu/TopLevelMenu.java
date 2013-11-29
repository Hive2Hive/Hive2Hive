package org.hive2hive.core.client.menu;

import org.hive2hive.core.client.H2HConsole;
import org.hive2hive.core.client.SessionInstance;
import org.hive2hive.core.process.IProcess;

public class TopLevelMenu extends ConsoleMenu {

	public TopLevelMenu(H2HConsole console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void addMenuItems() {
					
		add(new H2HConsoleMenuItem("Network Configuration") {
			protected void execute() {
				networkConfigurationHandler();
			}
		});
		add(new H2HConsoleMenuItem("User Configuration") {
			protected void execute() {
				userConfigurationHandler();
			}
		});
		add(new H2HConsoleMenuItem("Register") {
			protected void execute() {
				registerHandler();
			}
		});
		add(new H2HConsoleMenuItem("Login") {
			protected void execute() {
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
