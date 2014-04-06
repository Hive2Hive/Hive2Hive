package org.hive2hive.client.menu;

import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
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
				nodeMenu.checkNetwork();
				userMenu.checkUserCredentials();
				fileMenu.checkRootDirectory();
			}

			protected void execute() throws NoPeerConnectionException, InterruptedException,
					InvalidProcessStateException {

				// check if registration required
				if (!nodeMenu.getNode().getUserManager().isRegistered(userMenu.getUserCredentials())) {
					System.out.println("This is your first visit on this network. You are now being registered.");
					// register
					IProcessComponent registerProcess = nodeMenu.getNode().getUserManager()
							.register(userMenu.getUserCredentials());
					executeBlocking(registerProcess, "Register");
				}
				
				// login
				IProcessComponent loginProcess = nodeMenu.getNode().getUserManager()
						.login(userMenu.getUserCredentials(), fileMenu.getRootDirectory().toPath());
				executeBlocking(loginProcess, "Login");
				
				if (nodeMenu.getNode().getUserManager().isLoggedIn(userMenu.getUserCredentials().getUserId())) {
					fileMenu.open(isExpertMode);
				} else {
					printError("Login failed.");
				}
			}
		});
		
		// TODO only add if logged in
		add(new H2HConsoleMenuItem("Logout") {
			protected void checkPreconditions() {
				nodeMenu.checkNetwork();
				userMenu.checkUserCredentials();
			}
			
			protected void execute() throws Exception {
				if (nodeMenu.getNode().getUserManager().isLoggedIn(userMenu.getUserCredentials().getUserId())) {
					IProcessComponent logoutProcess = nodeMenu.getNode().getUserManager().logout();
					executeBlocking(logoutProcess, "Logout");
				} else {
					System.out.println("Logout is not possible. You are not logged in.");
				}
			}
		});
		
		// TODO add option to go into FileMenu (not only after login)
	}

	@Override
	public String getInstruction() {
		return "Please select an option:";
	}
}
