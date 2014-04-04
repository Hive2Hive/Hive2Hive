package org.hive2hive.client.menu;

import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.core.api.interfaces.IH2HNode;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;

public final class H2HConsoleMenuItemFactory {
	
	public H2HConsoleMenuItem createLoginItem(final IH2HNode node) {
		
		return new H2HConsoleMenuItem("Login") {
			@Override
			protected void checkPreconditions() {
//				if (node == null) {
//					printPreconditionError("Cannot login: Please connect to a network first.");
//					nodeMenu.open();
//				}
//				if (userMenu.getUserCredentials() == null) {
//					printPreconditionError("Cannot login: Please create UserCredentials first.");
//					userMenu.CreateUserCredentials.invoke();
//				}
//				if (root == null) {
//					root = new File(FileUtils.getUserDirectory(), "H2H_" + System.currentTimeMillis());
//					System.out.printf("Specify root path or enter 'ok' if you agree to: %s", root.toPath());
//					String input = awaitStringParameter();
//					if (!input.equalsIgnoreCase("ok"))
//						root = new File(input);
//					if (!Files.exists(root.toPath(), LinkOption.NOFOLLOW_LINKS)) {
//						try {
//							FileUtils.forceMkdir(root);
//						} catch (Exception e) {
//							printError(String.format("Exception on creating the root directory %s: " + e,
//									root.toPath()));
//							checkPreconditions();
//						}
//					}
//				}
			}

			protected void execute() throws NoPeerConnectionException, InterruptedException,
					InvalidProcessStateException {
//				IProcessComponent process = node.getUserManager().login(userMenu.getUserCredentials(), root.toPath());
//				executeBlocking(process);
			}
		};
	}
}
