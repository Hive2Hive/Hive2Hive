package org.hive2hive.client.menu;

import org.hive2hive.client.console.ConsoleMenuItem;
import org.hive2hive.client.console.H2HConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.client.util.MenuContainer;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

public final class RootMenu extends H2HConsoleMenu {

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

		add(new H2HConsoleMenuItem("Login") {
			protected boolean checkPreconditions() throws NoPeerConnectionException,
					InvalidProcessStateException, InterruptedException {
				if (!menus.getNodeMenu().createNetwork()) {
					printAbortion(displayText, "Node not connected.");
					return false;
				}
				if (!menus.getUserMenu().createUserCredentials()) {
					printAbortion(displayText, "User credentials not specified.");
					return false;
				}
				if (!menus.getFileMenu().createRootDirectory()) {
					printAbortion(displayText, "Root directory not specified.");
					return false;
				}
				if (!register()) {
					printAbortion(displayText, "Registering failed.");
					return false;
				}
				return true;
			}

			protected void execute() throws NoPeerConnectionException, InterruptedException,
					InvalidProcessStateException {

				register();

				IProcessComponent loginProcess = menus
						.getNodeMenu()
						.getNode()
						.getUserManager()
						.login(menus.getUserMenu().getUserCredentials(),
								menus.getFileMenu().getRootDirectory().toPath());

				// TODO find a cleaner way to handle login failures
				boolean success = executeBlocking(loginProcess, displayText);
				if (!success) {
					menus.getUserMenu().reset();
					menus.getFileMenu().reset();
				}
			}
		});

		add(new H2HConsoleMenuItem("Logout") {
			protected boolean checkPreconditions() throws Exception {
				return checkLogin();
			}

			protected void execute() throws Exception {

				IProcessComponent logoutProcess = menus.getNodeMenu().getNode().getUserManager().logout();
				executeBlocking(logoutProcess, displayText);
			}
		});

		add(new H2HConsoleMenuItem("File Menu") {
			@Override
			protected boolean checkPreconditions() throws Exception {
				return checkLogin();
			}

			protected void execute() throws Exception {
				menus.getFileMenu().open(isExpertMode);
			}
		});
	}

	@Override
	public String getInstruction() {
		return "Please select an option:";
	}

	private boolean register() throws NoPeerConnectionException, InvalidProcessStateException,
			InterruptedException {

		if (menus.getNodeMenu().getNode().getUserManager()
				.isRegistered(menus.getUserMenu().getUserCredentials().getUserId())) {
			return true;
		} else {
			H2HConsoleMenuItem
					.printPrecondition("You are not registered to the network. This will now happen automatically.");
			IProcessComponent registerProcess = menus.getNodeMenu().getNode().getUserManager()
					.register(menus.getUserMenu().getUserCredentials());
			return executeBlocking(registerProcess, "Register");
		}
	}

	private boolean checkLogin() throws NoPeerConnectionException {

		if (menus.getNodeMenu().getNode() == null) {
			H2HConsoleMenuItem
					.printPrecondition("You are not logged in. Node is not connected to a network.");
			return false;
		}
		if (menus.getUserMenu().getUserCredentials() == null) {
			H2HConsoleMenuItem.printPrecondition("You are not logged in. No user credentials specified.");
			return false;
		}
		if (!menus.getNodeMenu().getNode().getUserManager()
				.isLoggedIn(menus.getUserMenu().getUserCredentials().getUserId())) {
			H2HConsoleMenuItem.printPrecondition("You are not logged in.");
			return false;
		}
		return true;
	}
}
