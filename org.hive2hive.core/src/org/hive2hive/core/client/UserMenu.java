package org.hive2hive.core.client;

import org.hive2hive.core.security.UserCredentials;

public class UserMenu extends ConsoleMenu {

	public UserMenu(H2HConsole console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void addMenuHandlers() {

		add("Set User ID", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set User ID");
				System.out.println("Specify the user ID:");
				session.setUserId(awaitStringParameter());
			}
		});
		add("Set User Password", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set User Password");
				System.out.println("Specify the user password:");
				session.setUserId(awaitStringParameter());
			}
		});
		add("Set User PIN", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Set User PIN");
				System.out.println("Specify the user PIN:");
				session.setUserId(awaitStringParameter());
			}
		});
		add("Create User Credentials", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Create User Credentials");
				session.setCredentials(new UserCredentials(session.getUserId(), session.getPassword(),
						session.getPin()));
			}
		});
	}

	@Override
	protected String getInstruction() {
		return "Please select a user configuration option:\n";
	}
}
