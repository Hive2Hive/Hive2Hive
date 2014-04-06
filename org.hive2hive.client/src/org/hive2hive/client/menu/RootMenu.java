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
				
				if (nodeMenu.getNode().getUserManager().isLoggedIn(userMenu.getUserCredentials().getUserId())) {
					new FileMenu().open(isExpertMode);
				} else {
					printError("Login failed.");
				}
			}
		});
		
		add(new H2HConsoleMenuItem("Logout") {
			protected void execute() throws Exception {
				// TODO check if logged in
			}
		});
	}

	private void executeBlocking(IProcessComponent process, String itemName) throws InterruptedException,
			InvalidProcessStateException {
		
		Formatter.setExecutionForeground();
		System.out.println(String.format("Executing '%s'...", itemName));
		process.start().await();
		Formatter.setDefaultForeground();
	}

	@Override
	public String getInstruction() {
		return "Please select an option:";
	}
}
