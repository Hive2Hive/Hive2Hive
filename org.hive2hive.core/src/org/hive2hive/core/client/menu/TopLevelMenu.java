package org.hive2hive.core.client.menu;

import org.hive2hive.core.client.ConsoleClient;
import org.hive2hive.core.client.SessionInstance;
import org.hive2hive.core.client.console.Console;
import org.hive2hive.core.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.listener.ProcessListener;

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
			protected void checkPreconditions() {
				if (session.getH2HNode() == null){
					printPreconditionError("Cannot register: Please create a H2HNode first.");
					new NetworkMenu(console, session).CreateH2HNodeMenutItem.invoke();
				}
				if (session.getCredentials() == null) {
					printPreconditionError("Cannot register: Please create UserCredentials first.");
					new UserMenu(console, session).CreateUserCredentials.invoke();
				}
			}
			protected void execute() {
				IProcess registerProcess = session.getH2HNode().register(session.getCredentials());
				ProcessListener processListener = new ProcessListener();
				registerProcess.addListener(processListener);
				while (!processListener.hasFinished()){
					// busy waiting
				}
			}
		});
		add(new H2HConsoleMenuItem("Login") {
			@Override
			protected void checkPreconditions() {
				if (session.getH2HNode() == null){
					printPreconditionError("Cannot register: Please create a H2HNode first.");
					new NetworkMenu(console, session).CreateH2HNodeMenutItem.invoke();
				}
				if (session.getCredentials() == null) {
					printPreconditionError("Cannot register: Please create UserCredentials first.");
					new UserMenu(console, session).CreateUserCredentials.invoke();
				}
			}
			protected void execute() {
				IProcess loginProcess = session.getH2HNode().login(session.getCredentials());
				ProcessListener processListener = new ProcessListener();
				loginProcess.addListener(processListener);
				while(!processListener.hasFinished()){
					// busy waiting
				}
			}
		});
		add(new H2HConsoleMenuItem("Add File") {
			protected void execute() {
				notImplemented();
			}
		});
		add(new H2HConsoleMenuItem("Update File") {
			protected void execute() {
				notImplemented();
			}
		});
		add(new H2HConsoleMenuItem("Delete File") {
			protected void execute() {
				notImplemented();
			}
		});
		add(new H2HConsoleMenuItem("Logout") {
			protected void execute() {
				notImplemented();
			}
		});
		add(new H2HConsoleMenuItem("Unregister") {
			protected void execute() {
				notImplemented();
			}
		});
	}
	
	private void notImplemented() {
		System.out.println("This option has not yet been implemented.\n");
	}
	
	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
