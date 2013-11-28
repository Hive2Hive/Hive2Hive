package org.hive2hive.core.client;

/**
 * The console menu for the register process options.
 * 
 * @author Christian
 * 
 */
public class RegisterMenu extends ConsoleMenu {

	public RegisterMenu(H2HConsole console) {
		super(console);
	}

	@Override
	protected void addMenuHandlers() {

		add("Register", new IConsoleMenuCallback() {
			public void invoke() {

			}
		});
	}

	@Override
	public String getInstruction() {
		return "Please select a register option.";
	}
}
