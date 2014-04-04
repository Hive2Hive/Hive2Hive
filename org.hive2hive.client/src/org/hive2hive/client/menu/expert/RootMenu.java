package org.hive2hive.client.menu.expert;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.menu.NodeMenu;
import org.hive2hive.client.util.Formatter;
import org.hive2hive.core.processes.framework.concretes.ProcessComponentListener;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public final class RootMenu extends H2HConsoleMenu {

	private final NodeMenu nodeMenu;

	public RootMenu(NodeMenu nodeMenu) {
		this.nodeMenu = nodeMenu;
		
	}

	@Override
	protected void createItems() {
//		
	}

	@Override
	protected void addMenuItems() {
		add(new H2HConsoleMenuItem("Connect") {
			protected void execute() {
				nodeMenu.open(isExpertMode);
			}
		});
//
//		add(new H2HConsoleMenuItem("User Configuration") {
//			protected void execute() {
//				userMenu.open();
//			}
//		});
//
//		add(new H2HConsoleMenuItem("Register") {
//			protected void checkPreconditions() {
//				if (node == null) {
//					printPreconditionError("Cannot register: Please create a H2HNode first.");
//					nodeMenu.open();
//				}
//				if (userMenu.getUserCredentials() == null) {
//					printPreconditionError("Cannot register: Please create UserCredentials first.");
//					userMenu.CreateUserCredentials.invoke();
//				}
//			}
//
//			protected void execute() throws NoPeerConnectionException, InterruptedException,
//					InvalidProcessStateException {
//				IProcessComponent process = node.getUserManager()
//						.register(userMenu.getUserCredentials());
//				executeBlocking(process);
//			}
//		});
//
//		add(Login);
//
//		add(new H2HConsoleMenuItem("Add File") {
//			@Override
//			protected void execute() throws Hive2HiveException, InterruptedException {
//				IProcessComponent process = node.getFileManager().add(askForFile(true));
//				executeBlocking(process);
//			}
//		});
//
//		add(new H2HConsoleMenuItem("Update File") {
//			protected void execute() throws Hive2HiveException, InterruptedException {
//				IProcessComponent process = node.getFileManager().update(askForFile(true));
//				executeBlocking(process);
//			}
//		});
//		add(new H2HConsoleMenuItem("Move File") {
//			protected void execute() throws Hive2HiveException, InterruptedException {
//				System.out.println("Source path needed: ");
//				File source = askForFile(true);
//
//				System.out.println("Destination path needed: ");
//				File destination = askForFile(false);
//
//				IProcessComponent process = node.getFileManager().move(source, destination);
//				executeBlocking(process);
//			}
//		});
//		add(new H2HConsoleMenuItem("Delete File") {
//			protected void execute() throws Hive2HiveException, InterruptedException {
//				IProcessComponent process = node.getFileManager().delete(askForFile(false));
//				executeBlocking(process);
//			}
//		});
//		add(new H2HConsoleMenuItem("Recover File") {
//			protected void execute() throws Hive2HiveException {
//				// TODO implement recover process
//				notImplemented();
//			}
//		});
//		add(new H2HConsoleMenuItem("Share") {
//			protected void execute() throws IllegalArgumentException, NoSessionException,
//					IllegalFileLocation, NoPeerConnectionException, InterruptedException,
//					InvalidProcessStateException {
//				System.out.println("Specify the folder to share:");
//				File folder = askForFile(true);
//
//				System.out.println("Who do you want to share with?");
//				String friendId = awaitStringParameter();
//
//				System.out.println("Read or write permissions? Enter 1 for 'READ-ONLY', 2 for WRITE");
//				int permission = awaitIntParameter();
//				PermissionType perm = PermissionType.WRITE;
//				if (permission == 1) {
//					perm = PermissionType.READ;
//				}
//
//				IProcessComponent process = node.getFileManager()
//						.share(folder, friendId, perm);
//				executeBlocking(process);
//			}
//		});
//		add(new H2HConsoleMenuItem("Get File list") {
//			protected void execute() throws Hive2HiveException, InterruptedException {
//				IResultProcessComponent<List<Path>> process = node.getFileManager()
//						.getFileList();
//				IProcessResultListener<List<Path>> resultListener = new IProcessResultListener<List<Path>>() {
//					@Override
//					public void onResultReady(List<Path> result) {
//						// print the digest
//						System.out.println("File List:");
//						for (Path path : result) {
//							System.out.println("* " + path.toString());
//						}
//					}
//				};
//
//				process.attachListener(resultListener);
//				executeBlocking(process);
//			}
//		});
//
//		add(new H2HConsoleMenuItem("File Observer") {
//			protected void checkPreconditions() {
//				if (root == null) {
//					printPreconditionError("Cannot configure file observer: Root path not defined yet. Please login first.");
//					Login.invoke();
//				}
//				if (node == null) {
//					printPreconditionError("Cannot register: Please create a H2HNode first.");
//					nodeMenu.open();
//					checkPreconditions();
//				}
//			}
//
//			@Override
//			protected void execute() throws Exception {
//				fileObserverMenu = new FileObserverMenu(root, node.getFileManager());
//				fileObserverMenu.open();
//			}
//		});
//
//		add(new H2HConsoleMenuItem("Logout") {
//			protected void execute() throws Hive2HiveException, InterruptedException {
//				IProcessComponent process = node.getUserManager().logout();
//				executeBlocking(process);
//			}
//		});

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
	private void executeBlocking(IProcessComponent process) throws InterruptedException,
			InvalidProcessStateException {
		final ProcessComponentListener processListener = new ProcessComponentListener();
		process.attachListener(processListener);
		process.start();

		System.out.println("Executing... " + process.getClass().getSimpleName());
		Formatter.setExecutionForeground();

		final CountDownLatch latch = new CountDownLatch(1);
		ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
		ScheduledFuture<?> handle = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				// check for completion
				if (processListener.hasFinished())
					latch.countDown();
			}
		}, 0, 500, TimeUnit.MILLISECONDS);

		// blocking wait for completion
		latch.await();
		handle.cancel(true);

		Formatter.setDefaultForeground();
	}

//	/**
//	 * Asks for a (valid) file
//	 */
//	private File askForFile(boolean expectExistence) {
//		File file = null;
//		do {
//			System.out.println("Specify the relative file path to " + root.getAbsolutePath());
//			String path = awaitStringParameter();
//			file = new File(root, path);
//			if (expectExistence && !file.exists())
//				System.out.println("File '" + file.getAbsolutePath() + "' does not exist. Try again.");
//		} while (expectExistence && (file == null || !file.exists()));
//		return file;
//	}

	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
