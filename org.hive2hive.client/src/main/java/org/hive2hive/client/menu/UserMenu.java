package org.hive2hive.client.menu;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;

import org.apache.commons.io.FileUtils;
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

	private H2HConsoleMenuItem createRootDirectory;
	private File rootDirectory;

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

		createRootDirectory = new H2HConsoleMenuItem("Create Root Directory") {
			protected void execute() throws Exception {

				rootDirectory = new File(FileUtils.getUserDirectory(), "H2H_"
						+ menus.getUserMenu().getUserCredentials().getUserId() + "_" + System.currentTimeMillis());

				if (isExpertMode) {
					print(String.format("Please specify the root directory path or enter 'ok' if you agree with '%s'.",
							rootDirectory.toPath()));

					String input = awaitStringParameter();

					if (!"ok".equalsIgnoreCase(input)) {
						// override the auto root directory
						rootDirectory = new File(input);
					}
				}

				if (!Files.exists(rootDirectory.toPath(), LinkOption.NOFOLLOW_LINKS)) {
					try {
						FileUtils.forceMkdir(rootDirectory);
						print(String.format("Root directory '%s' created.", rootDirectory));
					} catch (Exception e) {
						printError(String
								.format("Exception on creating the root directory %s: " + e, rootDirectory.toPath()));
					}
				} else {
					print(String.format("Existing root directory '%s' will be used.", rootDirectory));
				}
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
		rootDirectory = null;
		ConsoleMenu.print("Root directory path has been reset.");
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

	public File getRootDirectory() {
		return rootDirectory;
	}

	public boolean createRootDirectory() {
		while (getRootDirectory() == null) {
			createRootDirectory.invoke();
		}
		// at this point, a root directory has always been specified
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
