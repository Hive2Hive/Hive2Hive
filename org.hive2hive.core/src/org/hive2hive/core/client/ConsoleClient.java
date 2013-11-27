package org.hive2hive.core.client;

import java.util.Scanner;

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
	private static Scanner scanner;

	public static void main(String[] args) {

//		h2hNode = H2HNodeBuilder.buildDefault();
		
		console = new H2HConsole();
		scanner = new Scanner(System.in);

		System.out.println("Welcome to the Hive2Hive console client!");

		TopLevelMenu menu = new TopLevelMenu(console);
		menu.open();

		System.exit(0);
	}
}