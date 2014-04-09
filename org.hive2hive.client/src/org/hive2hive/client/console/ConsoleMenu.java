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

	private final String exitToken = "Q";
	private boolean exited;

	public ConsoleMenu() {
		this.items = new ArrayList<ConsoleMenuItem>();
		createItems();
	}

	/**
	 * Specifies the {@link H2HConsoleMenuItem}s of this menu.<br/>
	 * <b>Note:</b> Not all {@link H2HConsoleMenuItem}s are specified here, as they might also be specified
	 * in-line in {@link ConsoleMenu#addMenuItems()}.</br>
	 * <b>Note:</b> {@link H2HConsoleMenuItem}s with preconditions should be specified by this method.
	 */
	protected void createItems() {
		// do nothing by default
	}

	/**
	 * Enlists the {@link H2HConsoleMenuItem}s of this menu.
	 */
	protected abstract void addMenuItems();

	protected String getExitItemText() {
		return "Back";
	}

	protected final void add(ConsoleMenuItem menuItem) {
		items.add(menuItem);
	}

	public final void open() {

		items.clear();
		addMenuItems();

		this.exited = false;
		while (!exited) {
			show();
		}
		onMenuExit();
	}

	private final void show() {

		System.out.println(getInstruction());
		System.out.println();

		// print normal items
		for (int i = 0; i < items.size(); ++i) {
			System.out.println(String.format("\t[%s]  %s", i + 1, items.get(i).getDisplayText()));
		}

		// print exit item
		System.out.println(String.format("\n\t[%s]  %s", exitToken, getExitItemText()));

		// evaluate input
		String input = exitToken;
		boolean validInput = false;

		while (!validInput) {
			input = awaitStringParameter();
			if (input.equalsIgnoreCase(exitToken)) {
				validInput = true;
				exit();
			} else {
				try {
					int chosen = Integer.valueOf(input);
					if (chosen > items.size() || chosen < 1) {
						printError(String.format("Invalid option. Please select an option from 1 to %s.",
								items.size()));
						validInput = false;
					} else {
						items.get(chosen - 1).invoke();
						validInput = true;
					}
				} catch (NumberFormatException e) {
					printError(String
							.format("This was not a valid input. Please select an option from 1 to %s or press '%s' to exit this menu.",
									items.size(), exitToken));
					validInput = false;
				}
			}
		}
	}

	public static String awaitStringParameter() {
		Formatter.setInputForeground();

		// do not close input
		@SuppressWarnings("resource")
		Scanner input = new Scanner(System.in);

		String parameter = null;
		boolean success = false;
		while (!success) {
			try {
				parameter = input.next();
				success = true;
			} catch (Exception e) {
				printError("Exception while parsing the parameter.");
			}
		}

		Formatter.setDefaultForeground();
		return parameter;
	}

	public static int awaitIntParameter() {
		boolean success = false;
		int number = 0;
		while (!success) {
			try {
				number = Integer.parseInt(awaitStringParameter());
				success = true;
			} catch (NumberFormatException e) {
				printError("This was not a number... Try again!");
			}
		}
		return number;
	}

	public static boolean awaitBooleanParameter() {
		return Boolean.parseBoolean(awaitStringParameter());
	}

	public static void printError(Throwable error) {
		error.printStackTrace();
	}
	
	public static void printError(String errorMsg) {
		Formatter.setErrorForeground();
		System.err.println(errorMsg);
		Formatter.setDefaultForeground();
	}

	protected void exit() {
		exited = true;
	}

	protected void onMenuExit() {
		// do nothing by default
	}

	protected abstract String getInstruction();
}
