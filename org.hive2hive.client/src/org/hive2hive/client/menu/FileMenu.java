package org.hive2hive.client.menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.core.exceptions.Hive2HiveException;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;
import org.hive2hive.core.processes.implementations.files.recover.IVersionSelector;

public class FileMenu extends H2HConsoleMenu {

	public H2HConsoleMenuItem CreateRootDirectory;

	private final NodeMenu nodeMenu;

	private File rootDirectory;

	public FileMenu(NodeMenu nodeMenu) {
		this.nodeMenu = nodeMenu;
	}

	@Override
	protected void createItems() {

		CreateRootDirectory = new H2HConsoleMenuItem("Create Root Directory") {
			protected void execute() throws Exception {

				// rootDirectory = new File(FileUtils.getUserDirectory(), "H2H_" +
				// System.currentTimeMillis());
				// TODO change
				rootDirectory = new File(FileUtils.getUserDirectory(), "Hive2Hive");

				if (isExpertMode) {
					System.out.printf(
							"Please specify the root directory path or enter 'ok' if you agree with '%s'.",
							rootDirectory.toPath());

					String input = awaitStringParameter();

					if (!input.equalsIgnoreCase("ok")) {
						while (!Files.exists(new File(input).toPath(), LinkOption.NOFOLLOW_LINKS)) {

							printError("This directory does not exist. Please retry.");
							input = awaitStringParameter();
						}
					}
				}

				if (!Files.exists(rootDirectory.toPath(), LinkOption.NOFOLLOW_LINKS)) {
					try {
						FileUtils.forceMkdir(rootDirectory);
					} catch (Exception e) {
						printError(String.format("Exception on creating the root directory %s: " + e,
								rootDirectory.toPath()));
					}
				}
			}
		};
	}

	@Override
	protected void addMenuItems() {

		// TODO add file observer

		add(new H2HConsoleMenuItem("Add File") {
			protected void checkPreconditions() {
				forceRootDirectory();
			}

			protected void execute() throws Hive2HiveException, InterruptedException {

				File file = askForFile(true);
				if (file == null)
					return;

				IProcessComponent addFileProcess = nodeMenu.getNode().getFileManager().add(file);
				executeBlocking(addFileProcess, displayText);
			}
		});

		add(new H2HConsoleMenuItem("Update File") {
			protected void checkPreconditions() {
				forceRootDirectory();
			}

			protected void execute() throws Hive2HiveException, InterruptedException {

				File file = askForFile(true);
				if (file == null)
					return;
				IProcessComponent updateFileProcess = nodeMenu.getNode().getFileManager().update(file);
				executeBlocking(updateFileProcess, displayText);
			}
		});

		add(new H2HConsoleMenuItem("Move File") {
			protected void checkPreconditions() {
				forceRootDirectory();
			}

			protected void execute() throws Hive2HiveException, InterruptedException {
				File source = askForFile(
						"Specify the relative path of the source file to the root directory '%s'.", true);
				if (source == null)
					return;
				File destination = askForFile(
						"Specify the relative path of the destination file to the root directory '%s'.",
						false); // TODO allow file destination that already exists
				if (destination == null)
					return;

				IProcessComponent moveFileProcess = nodeMenu.getNode().getFileManager()
						.move(source, destination);
				executeBlocking(moveFileProcess, displayText);
			}
		});

		add(new H2HConsoleMenuItem("Delete File") {
			protected void checkPreconditions() {
				forceRootDirectory();
			}
			protected void execute() throws Hive2HiveException, InterruptedException {
				
				File file = askForFile(true);
				if (file == null)
					return;
				
				IProcessComponent deleteFileProcess = nodeMenu.getNode().getFileManager().delete(file);
				executeBlocking(deleteFileProcess, displayText);
			}
		});
		
		add(new H2HConsoleMenuItem("Recover File") {
			protected void execute() throws Hive2HiveException, FileNotFoundException, IllegalArgumentException, InterruptedException {
				// TODO implement recover process
				File file = askForFile(true);
				if (file == null)
					return;
				
				IVersionSelector versionSelector = new IVersionSelector() {
					@Override
					public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
						return new VersionSelectionMenu(availableVersions).openAndSelect();
					}
				};
				
				IProcessComponent recoverFileProcess = nodeMenu.getNode().getFileManager().recover(file, versionSelector);
				executeBlocking(recoverFileProcess, displayText);
			}
		});
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
//				IProcessComponent process = node.getFileManager().share(folder, friendId, perm);
//				executeBlocking(process);
//			}
//		});
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

	private File askForFile(String msg, boolean expectExistence) {
		// TODO allow drag&drop or another kind of easy navigation
		// TODO find better way to exit this menu

		File file = null;
		do {
			System.out.println(String.format(
					msg.concat(expectExistence ? " The file at this path must exist." : ""),
					rootDirectory.getAbsolutePath()));
			System.out.println("Or enter 'cancel' in order to go back.");

			String input = awaitStringParameter();

			if (input.equalsIgnoreCase("cancel"))
				return null;

			file = new File(rootDirectory, input);
			if (expectExistence && !file.exists())
				System.out.println(String.format("The specified file '%s' does not exist. Try again.",
						file.getAbsolutePath()));
		} while (expectExistence && (file == null || !file.exists()));
		return file;

	}

	private File askForFile(boolean expectExistence) {
		return askForFile("Specify the relative path to the root directory '%s'.", expectExistence);
	}

	@Override
	protected String getInstruction() {
		return "Select a file operation:";
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	public void forceRootDirectory() {
		while (getRootDirectory() == null) {
			CreateRootDirectory.invoke();
		}
	}
}
