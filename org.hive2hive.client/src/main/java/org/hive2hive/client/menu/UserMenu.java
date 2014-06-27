package org.hive2hive.client.menu;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.util.MenuContainer;
import org.hive2hive.core.security.UserCredentials;

/**
 * The user configuration menu of the {@link ConsoleClient}.
 * 
 * @author Christian, Nico
 * 
 */
public final class UserMenu extends H2HConsoleMenu {

	private H2HConsoleMenuItem createUserCredentials;
	private UserCredentials userCredentials;

	public UserMenu(MenuContainer menus) {
		super(menus);
	}

	@Override
	protected void createItems() {
		createUserCredentials = new H2HConsoleMenuItem("Create User Credentials") {
			protected void execute() throws Exception {
				userCredentials = new UserCredentials(askUsedId(), askPassword(), askPin());
				exit();
			}
		};
	}

	@Override
	protected void addMenuItems() {
		add(createUserCredentials);
	}

	@Override
	protected String getInstruction() {
		return "Please select a user configuration option:";
	}
	
	@Override
	public void reset() {
		userCredentials = null;
		ConsoleMenu.print("User credentials have been reset.");
	}

	public UserCredentials getUserCredentials() {
		return userCredentials;
	}

	public boolean createUserCredentials() {
		while (getUserCredentials() == null) {
			createUserCredentials.invoke();
		}
		// at this point, credentials have always been specified
		return true;
	}

	private String askUsedId() {
		print("Specify the user ID:");
		return awaitStringParameter().trim();
	}

	private String askPassword() {
		print("Specify the user password:");
		return awaitStringParameter().trim();
	}

	private String askPin() {
		print("Specify the user PIN:");
		return awaitStringParameter().trim();
	}
}
