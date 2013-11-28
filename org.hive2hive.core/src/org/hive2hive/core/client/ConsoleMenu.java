package org.hive2hive.core.client;

import java.util.ArrayList;
import java.util.Scanner;

public abstract class ConsoleMenu {

	protected final H2HConsole console;
	protected final SessionInstance session;
	private final ArrayList<ConsoleMenuItem> items;

	private boolean exited;

	public ConsoleMenu(H2HConsole console, SessionInstance session) {
		this.console = console;
		this.session = session;
		this.items = new ArrayList<ConsoleMenuItem>();
		this.exited = false;

		addMenuHandlers();

		add("Exit", new IConsoleMenuCallback() {
			public void invoke() {
				printMenuSelection("Exit");
				exitHandler();
			}
		});
	}

	protected abstract void addMenuHandlers();

	protected final void add(String displayText, IConsoleMenuCallback callback) {
		items.add(new ConsoleMenuItem(displayText, callback));
	}

	public void open() {
		while (!exited) {
			console.clear();
			show();
		}
	}

	private final void show() {

		int chosen = 0;
		Scanner input = new Scanner(System.in);

		System.out.println(getInstruction());

		for (int i = 0; i < items.size(); ++i) {
			ConsoleMenuItem item = items.get(i);
			System.out.println(String.format("    [%s] %s", i + 1, item.getDisplayText()));
		}
		System.out.println();

		chosen = Integer.parseInt(awaitParameter());

		console.clear();

		if (chosen > items.size() || chosen < 1) {
			System.out.println(String.format("Invalid option. Select an option from 1 to %s.", items.size()));
			System.out.println("Press enter to continue...");
			input.nextLine();
			input.nextLine();
		} else {
			ConsoleMenuItem item = items.get(chosen - 1);
			IConsoleMenuCallback callback = item.getCallback();
			callback.invoke();
		}

		// do not close input
	}

	protected String awaitParameter() {
		
		Scanner input = new Scanner(System.in);
		String parameter;
		try {
			parameter = input.next();
		} catch (Exception e) {
			System.out.println("Exception while parsing the parameter.");
			input.nextLine();
			return null;
		}
		// do not close input
		
		return parameter;
	}
	
	private void exitHandler() {
		exited = true;
	}

	protected void printMenuSelection(String selectedOption) {
		System.out.println("Selected Option: " + selectedOption);
	}

	protected abstract String getInstruction();
}
