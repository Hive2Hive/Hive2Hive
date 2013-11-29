package org.hive2hive.core.client.menu;

import org.hive2hive.core.client.ConsoleClient;
import org.hive2hive.core.client.SessionInstance;
import org.hive2hive.core.client.console.Console;
import org.hive2hive.core.client.menuitem.H2HConsoleMenuItem;
import org.hive2hive.core.process.IProcess;
import org.hive2hive.core.process.listener.IProcessListener;

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
			@Override
			protected boolean preconditionsSatisfied() {
				if (session.getH2HNode() == null){
					printPreconditionError("Cannot register: Please create a H2HNode first.");
					new NetworkMenu(console, session).CreateH2HNodeMenutItem.invoke();
				}
				if (session.getCredentials() == null) {
					printPreconditionError("Cannot register: Please create UserCredentials first.");
					new UserMenu(console, session).CreateUserCredentials.invoke();
				}
				return true;
			}
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
		registerProcess.addListener(new IProcessListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFail(String reason) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	private void loginHandler() {
		
		
	}
	
	@Override
	public String getInstruction() {
		return "Please select an option:\n";
	}
}
