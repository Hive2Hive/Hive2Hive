package org.hive2hive.core.client.menu;

import java.util.ArrayList;
import java.util.Scanner;

import org.hive2hive.core.client.H2HConsole;
import org.hive2hive.core.client.SessionInstance;
import org.hive2hive.core.client.menuitem.ConsoleMenuItem;
import org.hive2hive.core.client.menuitem.H2HConsoleMenuItem;

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

		addMenuItems();

		add(new H2HConsoleMenuItem("Back") {
			protected void execute() {
				exitHandler();
			}
		});
	}

	protected abstract void addMenuItems();

	protected final void add(ConsoleMenuItem menuItem) {
		items.add(menuItem);
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
			System.out.println(String.format("    [%s]  %s", i + 1, item.getDisplayText()));
		}
		System.out.println();

		chosen = awaitIntParameter();

		console.clear();

		if (chosen > items.size() || chosen < 1) {
			System.out.println(String.format("Invalid option. Select an option from 1 to %s.", items.size()));
			System.out.println("Press enter to continue...");
			input.nextLine();
		} else {
			ConsoleMenuItem item = items.get(chosen - 1);
//			IConsoleMenuCallback callback = item.getCallback();
//			callback.invoke();
			item.invoke();
		}

		// do not close input
	}

	protected String awaitStringParameter() {
		
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
	
	protected int awaitIntParameter() {
		
		return Integer.parseInt(awaitStringParameter());
	}
	
	protected boolean awaitBooleanParameter() {
		return Boolean.parseBoolean(awaitStringParameter());
	}
	
	private void exitHandler() {
		exited = true;
	}

	protected abstract String getInstruction();
}
