package org.hive2hive.client;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.menu.IntroMenu;
import org.hive2hive.client.menu.LoggerMenu;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	public static void main(String[] args) {
		new ConsoleClient().start();
		System.exit(0);
	}

	public void start() {
		printHeader();
		printInstructions();

		new LoggerMenu().open();

		IntroMenu menu = new IntroMenu();
		menu.open();
		menu.shutdown();

		printFooter();
	}

	private static void printHeader() {
		ConsoleMenu.print("\n****** Welcome to the Hive2Hive console client! ******\n");
	}

	private static void printFooter() {
		ConsoleMenu.print("\n************** Goodbye! (hive2hive.com) **************\n");
	}

	private static void printInstructions() {
		ConsoleMenu.print("Configure and operate on your Hive2Hive network by following the guides.\n");
		ConsoleMenu.print("Navigate through the menus by entering the numbers next to the items of your choice.");
	}
}