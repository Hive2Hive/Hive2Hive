package org.hive2hive.core.client.menu;

import org.hive2hive.core.client.ConsoleClient;
import org.hive2hive.core.client.SessionInstance;
import org.hive2hive.core.client.console.Console;
import org.hive2hive.core.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.security.UserCredentials;

/**
 * The user configuration menu of the {@link ConsoleClient}.
 * 
 * @author Christian
 * 
 */
public final class UserMenu extends ConsoleMenu {

	public H2HConsoleMenuItem SetUserID;
	public H2HConsoleMenuItem SetUserPassword;
	public H2HConsoleMenuItem SetUserPin;
	public H2HConsoleMenuItem CreateUserCredentials;
	
	public UserMenu(Console console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void createItems() {
		SetUserID = new H2HConsoleMenuItem("Set User ID") {
			protected void execute() throws Exception {
				System.out.println("Specify the user ID:");
				session.setUserId(awaitStringParameter());
			}
		};
		SetUserPassword = new H2HConsoleMenuItem("Set User Password") {
			protected void execute() throws Exception {
				System.out.println("Specify the user password:");
				session.setPassword(awaitStringParameter());
			}
		};
		SetUserPin = new H2HConsoleMenuItem("Set User PIN") {
			protected void execute() throws Exception {
				System.out.println("Specify the user PIN:");
				session.setPin(awaitStringParameter());
			}
		};
		CreateUserCredentials = new H2HConsoleMenuItem("Create User Credentials") {
			@Override
			protected void checkPreconditions() {
				if (session.getUserId() == null) {
					printPreconditionError("User Credentials cannot be created: Please set User ID first.");
					SetUserID.invoke();
				}
				if (session.getPassword() == null) {
					printPreconditionError("User Credentials cannot be created: Please set User Password first.");
					SetUserPassword.invoke();
				}
				if (session.getPin() == null) {
					printPreconditionError("User Credentials cannot be created: Please set User PIN first.");
					SetUserPin.invoke();
				}
			}

			protected void execute() throws Exception {
				session.setCredentials(new UserCredentials(session.getUserId(), session.getPassword(),
						session.getPin()));
			}
		};
	}
	
	@Override
	protected void addMenuItems() {

		add(SetUserID);
		add(SetUserPassword);
		add(SetUserPin);
		add(CreateUserCredentials);
	}

	@Override
	protected String getInstruction() {
		return "Please select a user configuration option:\n";
	}
}
