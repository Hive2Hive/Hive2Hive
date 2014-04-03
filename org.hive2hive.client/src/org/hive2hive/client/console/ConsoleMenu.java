package org.hive2hive.client.console;

import java.util.ArrayList;
import java.util.Scanner;

import org.hive2hive.client.util.Formatter;

/**
 * An abstract console menu to be used with a {@link UIConsole}.
 * 
 * @author Christian
 * 
 */
public abstract class ConsoleMenu {

	private final ArrayList<ConsoleMenuItem> items;

	private boolean exited;

	public ConsoleMenu() {
		this.items = new ArrayList<ConsoleMenuItem>();

		setup();
	}
	
	protected void setup() {
		createItems();
		addMenuItems();

		add(new PreconditionConsoleMenuItem("Back") {
			protected void execute() {
				exit();
			}
		});
	}
	
	/**
	 * Specifies the {@link PreconditionConsoleMenuItem}s of this menu.<br/>
	 * <b>Note:</b> Not all {@link PreconditionConsoleMenuItem}s are specified here, as they might also be specified
	 * in-line in {@link ConsoleMenu#addMenuItems()}.</br>
	 * <b>Note:</b> {@link PreconditionConsoleMenuItem}s with preconditions should be specified by this method.
	 */
	protected void createItems() {
		// do nothing by default
	}

	/**
	 * Enlists the {@link PreconditionConsoleMenuItem}s of this menu.
	 */
	protected abstract void addMenuItems();

	protected final void add(ConsoleMenuItem menuItem) {
		items.add(menuItem);
	}

	public final void open() {
		this.exited = false;
		while (!exited) {
			show();
		}
		onMenuExit();
	}

	protected void onMenuExit() {
		// do nothing by default
	}

	private final void show() {
		int chosen = 0;

		System.out.println(getInstruction());

		for (int i = 0; i < items.size(); ++i) {
			ConsoleMenuItem item = items.get(i);
			System.out.println(String.format("    [%s]  %s", i + 1, item.getDisplayText()));
		}
		System.out.println();

		chosen = awaitIntParameter();

		if (chosen > items.size() || chosen < 1) {
			printError(String.format("Invalid option. Select an option from 1 to %s.\n", items.size()));
		} else {
			ConsoleMenuItem item = items.get(chosen - 1);
			item.invoke();
		}

		// do not close input
	}

	protected String awaitStringParameter() {
		Formatter.setInputForeground();

		// do not close input
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);

		String parameter;
		try {
			parameter = input.nextLine();
		} catch (Exception e) {
			printError("Exception while parsing the parameter.");
			try {
				input.nextLine();
			} catch (Exception ex) {
				// ignore
			}
			return null;
		}

		Formatter.setDefaultForeground();
		return parameter;
	}

	protected int awaitIntParameter() {
		boolean success = false;
		int number = 0;
		int tries = 0;
		while (!success && tries < 5) {
			try {
				number = Integer.parseInt(awaitStringParameter());
				success = true;
			} catch (NumberFormatException e) {
				tries++;
				printError("This was not a number... Try again! (" + (5 - tries) + " tries left)");
			}
		}
		return number;
	}

	protected boolean awaitBooleanParameter() {
		return Boolean.parseBoolean(awaitStringParameter());
	}

	protected void exit() {
		exited = true;
	}

	protected abstract String getInstruction();

	protected void printError(String errorMsg) {
		Formatter.setErrorForeground();
		System.err.println(errorMsg);
		Formatter.setDefaultForeground();
	}

	protected void notImplemented() {
		System.out.println("This option has not yet been implemented.\n");
	}
}
