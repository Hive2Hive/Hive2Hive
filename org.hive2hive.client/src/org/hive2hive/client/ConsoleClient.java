package org.hive2hive.client;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.menu.IntroMenu;
import org.hive2hive.client.util.LoggerInit;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	public static void main(String[] args) {
		new ConsoleClient().start();
	}

	public void start() {
		printHeader();
		printInstructions();

		LoggerInit.initLogger();

		new IntroMenu().open();

		printFooter();

		System.exit(0);
	}
	
	private static void printHeader() {		
		ConsoleMenu.print("\n************************************************************************************");
		ConsoleMenu.print("*      .´'`.                                                            .´'`.      *");
		ConsoleMenu.print("*      |   |                                                            |   |      *");
		ConsoleMenu.print("*    .´ `-´ `.        Welcome to the Hive2Hive console client!        .´ `-´ `.    *");
		ConsoleMenu.print("*    |   |   |                                                        |   |   |    *");
		ConsoleMenu.print("*     `-´ `-´                                                          `-´ `-´     *");
		ConsoleMenu.print("************************************************************************************\n");
	}
	
	private static void printFooter() {
		ConsoleMenu.print("\n************************************************************************************");
		ConsoleMenu.print("*      .´'`.                                                            .´'`.      *");
		ConsoleMenu.print("*      |   |                                                            |   |      *");
		ConsoleMenu.print("*    .´ `-´ `.                        Goodbye!                        .´ `-´ `.    *");
		ConsoleMenu.print("*    |   |   |                    (hive2hive.com)                     |   |   |    *");
		ConsoleMenu.print("*     `-´ `-´                                                          `-´ `-´     *");
		ConsoleMenu.print("************************************************************************************");
	}
	
	private static void printInstructions() {
		ConsoleMenu.print("Configure and operate on your Hive2Hive network by following the guides.\n");
		ConsoleMenu.print("Navigate through the menus by entering the numbers next to the items of your choice.\n");
	}
}