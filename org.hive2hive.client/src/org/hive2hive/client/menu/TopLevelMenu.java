package org.hive2hive.client.menu;

import java.io.File;

import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.console.Console;
import org.hive2hive.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.listener.ProcessListener;

/**
 * The top-level menu of the {@link ConsoleClient}.
 * 
 * @author Christian
 * 
 */
public final class TopLevelMenu extends ConsoleMenu {

	private final UserMenu userMenu;
	private final NodeCreationMenu networkMenu;

	public TopLevelMenu(Console console) {
		super(console);
		userMenu = new UserMenu(console);
		networkMenu = new NodeCreationMenu(console);
	}

	@Override
	protected void addMenuItems() {

		add(new H2HConsoleMenuItem("Network Configuration") {
			protected void execute() {
				networkMenu.open();
			}
		});
		add(new H2HConsoleMenuItem("User Configuration") {
			protected void execute() {
				userMenu.open();
			}
		});
		add(new H2HConsoleMenuItem("Register") {
			protected void checkPreconditions() {
				if (networkMenu.getH2HNode() == null) {
					printPreconditionError("Cannot register: Please create a H2HNode first.");
					networkMenu.open();
				}
				if (userMenu.getUserCredentials() == null) {
					printPreconditionError("Cannot register: Please create UserCredentials first.");
					userMenu.CreateUserCredentials.invoke();
				}
			}

			protected void execute() {
				IProcess registerProcess = networkMenu.getH2HNode().register(userMenu.getUserCredentials());
				ProcessListener processListener = new ProcessListener();
				registerProcess.addListener(processListener);
				while (!processListener.hasFinished()) {
					// busy waiting
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		add(new H2HConsoleMenuItem("Login") {
			@Override
			protected void checkPreconditions() {
				if (networkMenu.getH2HNode() == null) {
					printPreconditionError("Cannot register: Please create a H2HNode first.");
					new NodeCreationMenu(console).open();
				}
				if (userMenu.getUserCredentials() == null) {
					printPreconditionError("Cannot register: Please create UserCredentials first.");
					new UserMenu(console).CreateUserCredentials.invoke();
				}
			}

			protected void execute() {
				System.out.println("Specify root path: ");
				String input = awaitStringParameter();
				File root = new File(input);
				IProcess loginProcess = networkMenu.getH2HNode().login(userMenu.getUserCredentials(), root);
				ProcessListener processListener = new ProcessListener();
				loginProcess.addListener(processListener);
				while (!processListener.hasFinished()) {
					// busy waiting
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		add(new H2HConsoleMenuItem("Add File") {
			protected void execute() {
				notImplemented();
			}
		});
		add(new H2HConsoleMenuItem("Update File") {
			protected void execute() {
				notImplemented();
			}
		});
		add(new H2HConsoleMenuItem("Delete File") {
			protected void execute() {
				notImplemented();
			}
		});
		add(new H2HConsoleMenuItem("Logout") {
			protected void execute() {
				notImplemented();
			}
		});
		add(new H2HConsoleMenuItem("Unregister") {
			protected void execute() {
				notImplemented();
			}
		});
	}

	private void notImplemented() {
		System.out.println("This option has not yet been implemented.\n");
	}

	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
