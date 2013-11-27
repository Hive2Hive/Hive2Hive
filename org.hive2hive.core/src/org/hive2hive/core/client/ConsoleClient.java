package org.hive2hive.core.client;

import java.util.Scanner;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	private static H2HConsole console;
	private static Scanner scanner;
	
	public static void main(String[] args) {

		console = new H2HConsole();
		scanner = new Scanner(System.in);
	}
}
