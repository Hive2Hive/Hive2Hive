package org.hive2hive.client.menu.expert;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItemFactory;
import org.hive2hive.core.security.UserCredentials;

/**
 * The user configuration menu of the {@link ConsoleClient}.
 * 
 * @author Christian, Nico
 * 
 */
public final class UserMenu extends ConsoleMenu {

	public H2HConsoleMenuItemFactory SetUserID;
	public H2HConsoleMenuItemFactory SetUserPassword;
	public H2HConsoleMenuItemFactory SetUserPin;
	public H2HConsoleMenuItemFactory CreateUserCredentials;

	private String userId;
	private String password;
	private String pin;
	private UserCredentials userCredentials;

	public UserMenu() {
		// super(console);
	}

	@Override
	protected void createItems() {
		SetUserID = new H2HConsoleMenuItemFactory("Set User ID") {
			protected void execute() throws Exception {
				System.out.println("Specify the user ID:");
				userId = awaitStringParameter().trim();
			}
		};
		SetUserPassword = new H2HConsoleMenuItemFactory("Set User Password") {
			protected void execute() throws Exception {
				System.out.println("Specify the user password:");
				password = awaitStringParameter().trim();
			}
		};
		SetUserPin = new H2HConsoleMenuItemFactory("Set User PIN") {
			protected void execute() throws Exception {
				System.out.println("Specify the user PIN:");
				pin = awaitStringParameter().trim();
			}
		};
		CreateUserCredentials = new H2HConsoleMenuItemFactory("Create User Credentials") {
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
