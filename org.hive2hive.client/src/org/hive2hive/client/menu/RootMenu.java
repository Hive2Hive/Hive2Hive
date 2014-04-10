package org.hive2hive.client.menu;

import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.util.MenuContainer;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public final class RootMenu extends H2HConsoleMenu {

	// TODO when executing processes, check for exceptions/rollbacks
	public RootMenu(MenuContainer menus) {
		super(menus);
	}

	@Override
	protected void addMenuItems() {
		add(new H2HConsoleMenuItem("Connect") {
			protected void execute() {
				menus.getNodeMenu().open(isExpertMode);
			}
		});

		// TODO following menu items only if connected
		add(new H2HConsoleMenuItem("Login") {
			@Override
			protected void checkPreconditions() {
				menus.getNodeMenu().forceNetwork();
				menus.getUserMenu().forceUserCredentials();
				menus.getFileMenu().forceRootDirectory();
			}

			protected void execute() throws NoPeerConnectionException, InterruptedException,
					InvalidProcessStateException {

				forceRegistration();

				IProcessComponent loginProcess = menus
						.getNodeMenu()
						.getNode()
						.getUserManager()
						.login(menus.getUserMenu().getUserCredentials(),
								menus.getFileMenu().getRootDirectory().toPath());
				executeBlocking(loginProcess, displayText);
			}
		});

		// TODO following menu items only if logged in
		add(new H2HConsoleMenuItem("Logout") {
			protected void execute() throws Exception {

				if (checkLogin()) {
					IProcessComponent logoutProcess = menus.getNodeMenu().getNode().getUserManager().logout();
					executeBlocking(logoutProcess, displayText);
				}
			}
		});

		add(new H2HConsoleMenuItem("File Menu") {
			protected void execute() throws Exception {
				if (checkLogin()) {
					menus.getFileMenu().open(isExpertMode);
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
		while (!menus.getNodeMenu().getNode().getUserManager()
				.isRegistered(menus.getUserMenu().getUserCredentials().getUserId())) {
			H2HConsoleMenuItem.printPreconditionError("You are not registered.");
			IProcessComponent registerProcess = menus.getNodeMenu().getNode().getUserManager()
					.register(menus.getUserMenu().getUserCredentials());
			executeBlocking(registerProcess, "Register");
		}
	}

	private boolean checkLogin() throws NoPeerConnectionException {

		if (menus.getNodeMenu().getNode() == null
				|| menus.getUserMenu().getUserCredentials() == null
				|| !menus.getNodeMenu().getNode().getUserManager()
						.isLoggedIn(menus.getUserMenu().getUserCredentials().getUserId())) {
			H2HConsoleMenuItem.printPreconditionError("You are not logged in.");
			return false;
		}
		return true;
	}
}
