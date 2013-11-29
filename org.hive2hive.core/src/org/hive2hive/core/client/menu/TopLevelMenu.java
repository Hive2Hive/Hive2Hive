package org.hive2hive.core.client.menu;

import org.hive2hive.core.client.ConsoleClient;
import org.hive2hive.core.client.SessionInstance;
import org.hive2hive.core.client.console.Console;
import org.hive2hive.core.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.process.IProcess;

/**
 * The top-level menu of the {@link ConsoleClient}.
 * @author Christian
 *
 */
public final class TopLevelMenu extends ConsoleMenu {

	public TopLevelMenu(Console console, SessionInstance session) {
		super(console, session);
	}

	@Override
	protected void addMenuItems() {
					
		add(new H2HConsoleMenuItem("Network Configuration") {
			protected void execute() {
				new NetworkMenu(console, session).open();
			}
		});
		add(new H2HConsoleMenuItem("User Configuration") {
			protected void execute() {
				new UserMenu(console, session).open();
			}
		});
		add(new H2HConsoleMenuItem("Register") {
			protected void execute() {
				registerHandler();
			}
		});
		add(new H2HConsoleMenuItem("Login") {
			protected void execute() {
				loginHandler();
			}
		});
	}

	private void registerHandler() {

		IProcess registerProcess = session.getH2HNode().register(session.getCredentials());
	}

	private void loginHandler() {
		
		
	}
	
	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
