package org.hive2hive.client;

import java.io.IOException;

import org.hive2hive.client.menu.TopLevelMenu;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	// private static Console console;

	public static void main(String[] args) {

		// console = new Console("Hive2Hive Console");
		printHeader();

		try {
			H2HLoggerFactory.initFactory();
		} catch (IOException e) {
			System.out.println("H2HLoggerFactory could not be initialized.");
		}

		new TopLevelMenu().open();
		
		printFooter();

		System.exit(0);
	}

	private static void printHeader() {
		System.out.println("\n**************************************************************************************************");
		System.out.println("*                                                                           .´'`.                *");
		System.out.println("*                                                                           |   |                *");
		System.out.println("*                           Welcome to the Hive2Hive console client!      .´ `-´ `.              *");
		System.out.println("*                                                                         |   |   |              *");
		System.out.println("*                                                                          `-´ `-´               *");
		System.out.println("**************************************************************************************************");
		System.out.println("\nConfigure your H2H network and nodes by browsing through the menus and follow the guides.\n");
	}
	
	private static void printFooter() {
		System.out.println("**************************************************************************************************");
		System.out.println("*                                                                           .´'`.                *");
		System.out.println("*                                                                           |   |                *");
		System.out.println("*                                          Goodbye!                       .´ `-´ `.              *");
		System.out.println("*                                    (www.hive2hive.com)                  |   |   |              *");
		System.out.println("*                                                                          `-´ `-´               *");
		System.out.println("**************************************************************************************************\n");
	}
}