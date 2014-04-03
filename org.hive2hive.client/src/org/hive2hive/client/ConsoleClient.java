package org.hive2hive.client;

import org.fusesource.jansi.AnsiConsole;
import org.hive2hive.client.menu.IntroMenu;
import org.hive2hive.client.menu.advanced.AdvancedMenu;
import org.hive2hive.client.menu.basic.BasicMenu;
import org.hive2hive.client.util.Formatter;
import org.hive2hive.client.util.LoggerInit;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	public static void main(String[] args) {

		AnsiConsole.systemInstall();
//		Formatter.setDefaultForeground();
		printHeader();
		
		LoggerInit.initLogger();

		new IntroMenu().open();
		
		printFooter();
//		Formatter.reset();
		AnsiConsole.systemUninstall();

		System.exit(0);
	}

	private static void printHeader() {		
		System.out.println("\n**********************************************************************************");
		System.out.println("*      .´'`.                                                            .´'`.      *");
		System.out.println("*      |   |                                                            |   |      *");
		System.out.println("*    .´ `-´ `.        Welcome to the Hive2Hive console client!        .´ `-´ `.    *");
		System.out.println("*    |   |   |                                                        |   |   |    *");
		System.out.println("*     `-´ `-´                                                          `-´ `-´     *");
		System.out.println("************************************************************************************");
		System.out.println("\nConfigure and operate on you H2H network by following the guides.\n");
	}
	
	private static void printFooter() {
		System.out.println("\n**********************************************************************************");
		System.out.println("*      .´'`.                                                            .´'`.      *");
		System.out.println("*      |   |                                                            |   |      *");
		System.out.println("     .´ `-´ `.                        Goodbye!                        .´ `-´ `.    *");
		System.out.println("*    |   |   |                    (hive2hive.com)                     |   |   |    *");
		System.out.println("*     `-´ `-´                                                          `-´ `-´     *");
		System.out.println("************************************************************************************");
	}
}