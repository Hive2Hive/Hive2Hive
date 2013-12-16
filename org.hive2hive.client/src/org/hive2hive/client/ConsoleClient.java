package org.hive2hive.client;

import java.io.IOException;

import org.fusesource.jansi.AnsiConsole;
import org.hive2hive.client.menu.TopLevelMenu;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	public static void main(String[] args) {

		AnsiConsole.systemInstall();
		Formatter.setDefaultForeground();
		printHeader();

		try {
			H2HLoggerFactory.initFactory();
		} catch (IOException e) {
			System.err.println("H2HLoggerFactory could not be initialized.");
		}

		new TopLevelMenu().open();
		
		printFooter();
		Formatter.reset();
		AnsiConsole.systemUninstall();

		System.exit(0);
	}

	private static void printHeader() {		
		System.out.println("\n********************************************************************************************");
		System.out.println("*          .´'`.                                                            .´'`.          *");
		System.out.println("*          |   |                                                            |   |          *");
		System.out.println("*        .´ `-´ `.        Welcome to the Hive2Hive console client!        .´ `-´ `.        *");
		System.out.println("*        |   |   |                                                        |   |   |        *");
		System.out.println("*         `-´ `-´                                                          `-´ `-´         *");
		System.out.println("********************************************************************************************");
		System.out.println("\nConfigure your H2H network and nodes by browsing through the menus and follow the guides.\n");
		
		
		
	}
	
	private static void printFooter() {
		System.out.println("\n********************************************************************************************");
		System.out.println("*          .´'`.                                                            .´'`.          *");
		System.out.println("*          |   |                                                            |   |          *");
		System.out.println("*        .´ `-´ `.                        Goodbye!                        .´ `-´ `.        *");
		System.out.println("*        |   |   |                                                        |   |   |        *");
		System.out.println("*         `-´ `-´                                                          `-´ `-´         *");
		System.out.println("********************************************************************************************");
	}
}