package org.hive2hive.client.menu;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.client.ConsoleClient;
import org.hive2hive.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.digest.IGetDigestProcess;
import org.hive2hive.core.process.listener.ProcessListener;

/**
 * The top-level menu of the {@link ConsoleClient}.
 * 
 * @author Christian, Nico
 * 
 */
public final class TopLevelMenu extends ConsoleMenu {

	public H2HConsoleMenuItem Login;
	
	private final UserMenu userMenu;
	private final NodeCreationMenu nodeMenu;
	private FileObserverMenu fileObserverMenu;
	protected File root;

	public TopLevelMenu() {
		// super(console);
		userMenu = new UserMenu();
		nodeMenu = new NodeCreationMenu();
	}

	@Override
	protected void onMenuExit() {
		// shutdown network
		if (nodeMenu.getH2HNode() != null) {
			nodeMenu.getH2HNode().disconnect();
		}
		// shutdown file observer
		if (fileObserverMenu != null && fileObserverMenu.getWatcher() != null){
			try {
				fileObserverMenu.getWatcher().stop();
			} catch (Exception e) {
				printError(e.getMessage());
			}
		}
	}

	@Override
	protected void createItems() {
		Login = new H2HConsoleMenuItem("Login") {
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
				if (root == null) {
					root = new File(FileUtils.getUserDirectory(), "H2H_" + System.currentTimeMillis());
					System.out.printf("Specify root path or enter 'ok' if you agree to: %s", root.toPath());
					String input = awaitStringParameter();
					if (!input.equalsIgnoreCase("ok"))
						root = new File(input);
					if (!Files.exists(root.toPath(), LinkOption.NOFOLLOW_LINKS)){
						try {
							FileUtils.forceMkdir(root);
						} catch(Exception e){
							printError(String.format("Exception on creating the root directory %s: " + e, root.toPath()));
							checkPreconditions();
						}
					}
				}
			}

			protected void execute() {
				IProcess process = nodeMenu.getH2HNode().getUserManagement()
						.login(userMenu.getUserCredentials(), root.toPath());
				executeBlocking(process);
			}
		};
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
				IProcess process = nodeMenu.getH2HNode().getUserManagement()
						.register(userMenu.getUserCredentials());
				executeBlocking(process);
			}
		});
		
		add(Login);

		add(new H2HConsoleMenuItem("Add File") {
			@Override
			protected void execute() throws Hive2HiveException {
				IProcess process = nodeMenu.getH2HNode().getFileManagement().add(askForFile(true));
				executeBlocking(process);
			}
		});

		add(new H2HConsoleMenuItem("Update File") {
			protected void execute() throws Hive2HiveException {
				IProcess process = nodeMenu.getH2HNode().getFileManagement().update(askForFile(true));
				executeBlocking(process);
			}
		});
		add(new H2HConsoleMenuItem("Delete File") {
			protected void execute() throws Hive2HiveException {
				IProcess process = nodeMenu.getH2HNode().getFileManagement().delete(askForFile(true));
				executeBlocking(process);
			}
		});
		add(new H2HConsoleMenuItem("Move File") {
			protected void execute() throws Hive2HiveException {
				System.out.println("Source path needed: ");
				File source = askForFile(true);

				System.out.println("Destination path needed: ");
				File destination = askForFile(false);

				IProcess process = nodeMenu.getH2HNode().getFileManagement().move(source, destination);
				executeBlocking(process);
			}
		});

		add(new H2HConsoleMenuItem("Get Digest") {
			protected void execute() throws Hive2HiveException {
				IGetDigestProcess process = nodeMenu.getH2HNode().getFileManagement().getDigest();
				executeBlocking(process);

				// print the digest
				List<Path> digest = process.getDigest();
				System.out.println("Digest request resulted:");
				for (Path path : digest) {
					System.out.println("* " + path.toString());
				}
			}
		});
		add(new H2HConsoleMenuItem("File Observer") {
			protected void checkPreconditions() {
				if (root == null){
					printPreconditionError("Cannot configure file observer: Root Path not defined yet. Please login first.");
					Login.invoke();
				}
				if (nodeMenu.getH2HNode() == null) {
					printPreconditionError("Cannot register: Please create a H2HNode first.");
					nodeMenu.open();
					checkPreconditions();
				}
			}
			@Override
			protected void execute() throws Exception {
				fileObserverMenu = new FileObserverMenu(root, nodeMenu.getH2HNode());
				fileObserverMenu.open();
			}
		});

		add(new H2HConsoleMenuItem("Logout") {
			protected void execute() throws Hive2HiveException {
				IProcess process = nodeMenu.getH2HNode().getUserManagement().logout();
				executeBlocking(process);
			}
		});
		add(new H2HConsoleMenuItem("Unregister") {
			protected void execute() {
				notImplemented();
			}
		});
	}

	/**
	 * Executes the given process (autostart anyhow) and blocks until it is done
	 */
	private void executeBlocking(IProcess process) {
		ProcessListener processListener = new ProcessListener();
		process.addListener(processListener);

		while (!processListener.hasFinished()) {
			// busy waiting
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Asks for a (valid) file
	 */
	private File askForFile(boolean expectExistence) {
		File file = null;
		do {
			System.out.println("Specify the relative file path to " + root.getAbsolutePath());
			String path = awaitStringParameter();
			file = new File(root, path);
			if (expectExistence && !file.exists())
				System.out.println("File '" + file.getAbsolutePath() + "' does not exist. Try again");
		} while (expectExistence && (file == null || !file.exists()));
		return file;
	}

	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
