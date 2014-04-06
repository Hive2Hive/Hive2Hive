package org.hive2hive.client.menu;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.core.security.UserCredentials;

/**
 * The user configuration menu of the {@link ConsoleClient}.
 * 
 * @author Christian, Nico
 * 
 */
public final class UserMenu extends H2HConsoleMenu {

	public H2HConsoleMenuItem CreateUserCredentials;

	private UserCredentials userCredentials;
	
	@Override
	protected void createItems() {
		CreateUserCredentials = new H2HConsoleMenuItem("Create User Credentials") {
			
			@Override
			protected void execute() throws Exception {
				userCredentials = new UserCredentials(askUsedId(), askPassword(), askPin());
				
				exit();
			}
		};
	}

	@Override
	protected void addMenuItems() {
		add(CreateUserCredentials);
	}

	@Override
	protected String getInstruction() {
		return "Please select a user configuration option:";
	}

	public UserCredentials getUserCredentials() {
		return userCredentials;
	}
	
	private String askUsedId() {
		System.out.println("Specify the user ID:");
		return awaitStringParameter().trim();
	}
	
	private String askPassword() {
		System.out.println("Specify the user password:");
		return awaitStringParameter().trim();
	}
	
	private String askPin() {
		System.out.println("Specify the user PIN:");
		return awaitStringParameter().trim();
	}
	
	public void checkUserCredentials() {
		while (getUserCredentials() == null) {
			CreateUserCredentials.invoke();
		}
	}
}
