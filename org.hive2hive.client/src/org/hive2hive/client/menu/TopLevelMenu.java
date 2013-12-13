package org.hive2hive.client.menu;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.listener.ProcessListener;

/**
 * The top-level menu of the {@link ConsoleClient}.
 * 
 * @author Christian, Nico
 * 
 */
public final class TopLevelMenu extends ConsoleMenu {

	private final UserMenu userMenu;
	private final NodeCreationMenu nodeMenu;
	protected File root;

	public TopLevelMenu() {
//		super(console);
		userMenu = new UserMenu();
		nodeMenu = new NodeCreationMenu();
	}

	@Override
	protected void onMenuExit() {
		// shutdown network
		if (nodeMenu.getH2HNode() != null) {
			nodeMenu.getH2HNode().disconnect();
		}
	}
	
	@Override
	protected void addMenuItems() {
		add(new H2HConsoleMenuItem("Network Configuration") {
			protected void execute() {
				nodeMenu.open();
			}
		});

		add(new H2HConsoleMenuItem("User Configuration") {
			protected void execute() {
				userMenu.open();
			}
		});

		add(new H2HConsoleMenuItem("Register") {
			protected void checkPreconditions() {
				if (nodeMenu.getH2HNode() == null) {
					printPreconditionError("Cannot register: Please create a H2HNode first.");
					nodeMenu.open();
				}
				if (userMenu.getUserCredentials() == null) {
					printPreconditionError("Cannot register: Please create UserCredentials first.");
					userMenu.CreateUserCredentials.invoke();
				}
			}

			protected void execute() {
				IProcess registerProcess = nodeMenu.getH2HNode().register(userMenu.getUserCredentials());
				ProcessListener processListener = new ProcessListener();
				registerProcess.addListener(processListener);
				while (!processListener.hasFinished()) {
					// busy waiting
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}
		});
		add(new H2HConsoleMenuItem("Login") {
			@Override
			protected void checkPreconditions() {
				if (nodeMenu.getH2HNode() == null) {
					printPreconditionError("Cannot login: Please create a H2HNode first.");
					nodeMenu.open();
				}
				if (userMenu.getUserCredentials() == null) {
					printPreconditionError("Cannot login: Please create UserCredentials first.");
					userMenu.CreateUserCredentials.invoke();
				}
			}

			protected void execute() {
				root = new File(FileUtils.getUserDirectory(), "H2H_" + System.currentTimeMillis());
				System.out.println("Specify root path or enter 'ok' if you're ok with: ");
				System.out.println(root.getAbsolutePath());
				String input = awaitStringParameter();
				if (!input.equalsIgnoreCase("ok"))
					root = new File(input);
				IProcess loginProcess = nodeMenu.getH2HNode().login(userMenu.getUserCredentials(), root);
				ProcessListener processListener = new ProcessListener();
				loginProcess.addListener(processListener);
				while (!processListener.hasFinished()) {
					// busy waiting
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}
		});

		add(new H2HConsoleMenuItem("Add File") {
			@Override
			protected void execute() {
				try {
					IProcess process = nodeMenu.getH2HNode().add(askForFile());
					ProcessListener processListener = new ProcessListener();
					process.addListener(processListener);

					while (!processListener.hasFinished()) {
						// busy waiting
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
					}
				} catch (IllegalFileLocation | NoSessionException e) {
					System.out.println("Could not add the file. Reason: " + e.getMessage());
				}
			}
		});

		add(new H2HConsoleMenuItem("Update File") {
			protected void execute() {
				try {
					IProcess process = nodeMenu.getH2HNode().update(askForFile());
					ProcessListener processListener = new ProcessListener();
					process.addListener(processListener);

					while (!processListener.hasFinished()) {
						// busy waiting
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
					}
				} catch (IllegalArgumentException | NoSessionException e) {
					System.out.println("Could not update the file. Reason: " + e.getMessage());
				}
			}
		});
		add(new H2HConsoleMenuItem("Delete File") {
			protected void execute() {
				try {
					IProcess process = nodeMenu.getH2HNode().delete(askForFile());
					ProcessListener processListener = new ProcessListener();
					process.addListener(processListener);

					while (!processListener.hasFinished()) {
						// busy waiting
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
						}
					}
				} catch (IllegalArgumentException | NoSessionException e) {
					System.out.println("Could not delete the file. Reason: " + e.getMessage());
				}
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

	private File askForFile() {
		File file = null;
		while (file == null || !file.exists()) {
			System.out.println("Specify the relative file path to " + root.getAbsolutePath());
			String path = awaitStringParameter();
			file = new File(root, path);
			if (!file.exists())
				System.out.println("File '" + file.getAbsolutePath() + "' does not exist. Try again");
		}
		return file;
	}

	private void notImplemented() {
		System.out.println("This option has not yet been implemented.\n");
	}

	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
