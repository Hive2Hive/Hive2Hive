package org.hive2hive.core.client;

import org.hive2hive.core.H2HNode;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	private static H2HNode h2hNode;

	private static H2HConsole console;

	public static void main(String[] args) {

//		h2hNode = H2HNodeBuilder.buildDefault();
		
		console = new H2HConsole();

		System.out.println("Welcome to the Hive2Hive console client!\n");

		TopLevelMenu menu = new TopLevelMenu(console);
		menu.open();

		System.exit(0);
	}
}