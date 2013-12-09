package org.hive2hive.client.menu;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.console.Console;
import org.hive2hive.client.menuitem.H2HConsoleMenuItem;
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

	private String userId;
	private String password;
	private String pin;
	private UserCredentials userCredentials;

	public UserMenu(Console console) {
		super(console);
	}

	@Override
	protected void createItems() {
		SetUserID = new H2HConsoleMenuItem("Set User ID") {
			protected void execute() throws Exception {
				System.out.println("Specify the user ID:");
				userId = awaitStringParameter();
			}
		};
		SetUserPassword = new H2HConsoleMenuItem("Set User Password") {
			protected void execute() throws Exception {
				System.out.println("Specify the user password:");
				password = awaitStringParameter();
			}
		};
		SetUserPin = new H2HConsoleMenuItem("Set User PIN") {
			protected void execute() throws Exception {
				System.out.println("Specify the user PIN:");
				pin = awaitStringParameter();
			}
		};
		CreateUserCredentials = new H2HConsoleMenuItem("Create User Credentials") {
			@Override
			protected void checkPreconditions() {
				if (userId == null) {
					printPreconditionError("User Credentials cannot be created: Please set User ID first.");
					SetUserID.invoke();
				}
				if (password == null) {
					printPreconditionError("User Credentials cannot be created: Please set User Password first.");
					SetUserPassword.invoke();
				}
				if (pin == null) {
					printPreconditionError("User Credentials cannot be created: Please set User PIN first.");
					SetUserPin.invoke();
				}
			}

			protected void execute() throws Exception {
				userCredentials = new UserCredentials(userId, password, pin);
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

	public UserCredentials getUserCredentials() {
		return userCredentials;
	}
}
