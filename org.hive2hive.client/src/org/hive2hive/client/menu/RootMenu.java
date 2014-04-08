package org.hive2hive.client.menu;

import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public final class RootMenu extends H2HConsoleMenu {

	// TODO when executing processes, check for exceptions/rollbacks

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

		// TODO following menu items only if connected
		add(new H2HConsoleMenuItem("Login") {
			@Override
			protected void checkPreconditions() {
				nodeMenu.forceNetwork();
				userMenu.forceUserCredentials();
				fileMenu.forceRootDirectory();
			}

			protected void execute() throws NoPeerConnectionException, InterruptedException,
					InvalidProcessStateException {

				forceRegistration();

				IProcessComponent loginProcess = nodeMenu.getNode().getUserManager()
						.login(userMenu.getUserCredentials(), fileMenu.getRootDirectory().toPath());
				executeBlocking(loginProcess, displayText);
			}
		});

		// TODO following menu items only if logged in
		add(new H2HConsoleMenuItem("Logout") {
			protected void execute() throws Exception {

				if (checkLogin()) {
					IProcessComponent logoutProcess = nodeMenu.getNode().getUserManager().logout();
					executeBlocking(logoutProcess, displayText);
				}
			}
		});

		add(new H2HConsoleMenuItem("File Menu") {
			protected void execute() throws Exception {
				if (checkLogin()) {
					fileMenu.open(isExpertMode);
				}
			}
		});
	}

	@Override
	public String getInstruction() {
		return "Please select an option:";
	}

	private void forceRegistration() throws InvalidProcessStateException, InterruptedException,
			NoPeerConnectionException {
		while (!nodeMenu.getNode().getUserManager().isRegistered(userMenu.getUserCredentials().getUserId())) {
			H2HConsoleMenuItem.printPreconditionError("You are not registered.");
			IProcessComponent registerProcess = nodeMenu.getNode().getUserManager()
					.register(userMenu.getUserCredentials());
			executeBlocking(registerProcess, "Register");
		}
	}

	private boolean checkLogin() throws NoPeerConnectionException {

		if (nodeMenu.getNode() == null || userMenu.getUserCredentials() == null
				|| !nodeMenu.getNode().getUserManager().isLoggedIn(userMenu.getUserCredentials().getUserId())) {
			H2HConsoleMenuItem.printPreconditionError("You are not logged in.");
			return false;
		}
		return true;
	}
}
