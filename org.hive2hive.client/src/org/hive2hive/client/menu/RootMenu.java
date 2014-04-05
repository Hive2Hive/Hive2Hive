package org.hive2hive.client.menu;

import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.util.Formatter;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public final class RootMenu extends H2HConsoleMenu {

	private final NodeMenu nodeMenu;
	private final UserMenu userMenu;
	private final FileMenu fileMenu;

	public RootMenu(NodeMenu nodeMenu, UserMenu userMenu, FileMenu fileMenu) {
		this.nodeMenu = nodeMenu;
		this.userMenu = userMenu;
		this.fileMenu = fileMenu;
	}

	@Override
	protected void addMenuItems() {
		add(new H2HConsoleMenuItem("Connect") {
			protected void execute() {
				nodeMenu.open(isExpertMode);
			}
		});

		add(new H2HConsoleMenuItem("Login") {
			@Override
			protected void checkPreconditions() {
				if (nodeMenu.getNode() == null) {
					printPreconditionError("Cannot login: Please connect to a network first.");
					nodeMenu.open(isExpertMode);
				}
				if (userMenu.getUserCredentials() == null) {
					userMenu.CreateUserCredentials.invoke();
				}
				if (fileMenu.getRootDirectory() == null) {
					fileMenu.CreateRootDirectory.invoke();
				}
			}

			protected void execute() throws NoPeerConnectionException, InterruptedException,
					InvalidProcessStateException {

				// TODO wait for negative feedback that user isn't yet registered
				// TODO return specific NotRegisteredException

				// check if registration required
				if (!nodeMenu.getNode().getUserManager()
						.isRegistered(userMenu.getUserCredentials().getUserId())) {
					System.out.println("This is your first visit on this network. Please register first.");
					// register
					IProcessComponent registerProcess = nodeMenu.getNode().getUserManager()
							.register(userMenu.getUserCredentials());
					executeBlocking(registerProcess, "Register");
				}

				// login
				IProcessComponent loginProcess = nodeMenu.getNode().getUserManager()
						.login(userMenu.getUserCredentials(), fileMenu.getRootDirectory().toPath());
				executeBlocking(loginProcess, "Login");
			}
		});

		// add(new H2HConsoleMenuItem("Add File") {
		// @Override
		// protected void execute() throws Hive2HiveException, InterruptedException {
		// IProcessComponent process = node.getFileManager().add(askForFile(true));
		// executeBlocking(process);
		// }
		// });
		//
		// add(new H2HConsoleMenuItem("Update File") {
		// protected void execute() throws Hive2HiveException, InterruptedException {
		// IProcessComponent process = node.getFileManager().update(askForFile(true));
		// executeBlocking(process);
		// }
		// });
		// add(new H2HConsoleMenuItem("Move File") {
		// protected void execute() throws Hive2HiveException, InterruptedException {
		// System.out.println("Source path needed: ");
		// File source = askForFile(true);
		//
		// System.out.println("Destination path needed: ");
		// File destination = askForFile(false);
		//
		// IProcessComponent process = node.getFileManager().move(source, destination);
		// executeBlocking(process);
		// }
		// });
		// add(new H2HConsoleMenuItem("Delete File") {
		// protected void execute() throws Hive2HiveException, InterruptedException {
		// IProcessComponent process = node.getFileManager().delete(askForFile(false));
		// executeBlocking(process);
		// }
		// });
		// add(new H2HConsoleMenuItem("Recover File") {
		// protected void execute() throws Hive2HiveException {
		// // TODO implement recover process
		// notImplemented();
		// }
		// });
		// add(new H2HConsoleMenuItem("Share") {
		// protected void execute() throws IllegalArgumentException, NoSessionException,
		// IllegalFileLocation, NoPeerConnectionException, InterruptedException,
		// InvalidProcessStateException {
		// System.out.println("Specify the folder to share:");
		// File folder = askForFile(true);
		//
		// System.out.println("Who do you want to share with?");
		// String friendId = awaitStringParameter();
		//
		// System.out.println("Read or write permissions? Enter 1 for 'READ-ONLY', 2 for WRITE");
		// int permission = awaitIntParameter();
		// PermissionType perm = PermissionType.WRITE;
		// if (permission == 1) {
		// perm = PermissionType.READ;
		// }
		//
		// IProcessComponent process = node.getFileManager()
		// .share(folder, friendId, perm);
		// executeBlocking(process);
		// }
		// });
		// add(new H2HConsoleMenuItem("Get File list") {
		// protected void execute() throws Hive2HiveException, InterruptedException {
		// IResultProcessComponent<List<Path>> process = node.getFileManager()
		// .getFileList();
		// IProcessResultListener<List<Path>> resultListener = new IProcessResultListener<List<Path>>() {
		// @Override
		// public void onResultReady(List<Path> result) {
		// // print the digest
		// System.out.println("File List:");
		// for (Path path : result) {
		// System.out.println("* " + path.toString());
		// }
		// }
		// };
		//
		// process.attachListener(resultListener);
		// executeBlocking(process);
		// }
		// });
		//
		// add(new H2HConsoleMenuItem("File Observer") {
		// protected void checkPreconditions() {
		// if (root == null) {
		// printPreconditionError("Cannot configure file observer: Root path not defined yet. Please login first.");
		// Login.invoke();
		// }
		// if (node == null) {
		// printPreconditionError("Cannot register: Please create a H2HNode first.");
		// nodeMenu.open();
		// checkPreconditions();
		// }
		// }
		//
		// @Override
		// protected void execute() throws Exception {
		// fileObserverMenu = new FileObserverMenu(root, node.getFileManager());
		// fileObserverMenu.open();
		// }
		// });
		//
		// add(new H2HConsoleMenuItem("Logout") {
		// protected void execute() throws Hive2HiveException, InterruptedException {
		// IProcessComponent process = node.getUserManager().logout();
		// executeBlocking(process);
		// }
		// });

		// add(new H2HConsoleMenuItem("Get Status") {
		// protected void execute() throws Hive2HiveException {
		// IH2HNodeStatus status = nodeMenu.getH2HNode().getStatus();
		// System.out.println("Connected: " + status.isConnected());
		// if (status.isLoggedIn()) {
		// System.out.println("User ID: " + status.getUserId());
		// System.out.println("Root path: " + status.getRoot().getAbsolutePath());
		// } else {
		// System.out.println("Currently, nobody is logged in");
		// }
		// System.out.println("Number of processes: " + status.getNumberOfProcesses());
		// }
		// });
	}

	/**
	 * Executes the given (already autostarted) process and blocks until it is done
	 * 
	 * @throws InterruptedException
	 * @throws InvalidProcessStateException
	 */
	private void executeBlocking(IProcessComponent process, String itemName) throws InterruptedException,
			InvalidProcessStateException {
		
		Formatter.setExecutionForeground();
		System.out.println(String.format("Executing '%s'...", itemName));
		process.start().await();
		Formatter.setDefaultForeground();
	}

	// /**
	// * Asks for a (valid) file
	// */
	// private File askForFile(boolean expectExistence) {
	// File file = null;
	// do {
	// System.out.println("Specify the relative file path to " + root.getAbsolutePath());
	// String path = awaitStringParameter();
	// file = new File(root, path);
	// if (expectExistence && !file.exists())
	// System.out.println("File '" + file.getAbsolutePath() + "' does not exist. Try again.");
	// } while (expectExistence && (file == null || !file.exists()));
	// return file;
	// }

	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
