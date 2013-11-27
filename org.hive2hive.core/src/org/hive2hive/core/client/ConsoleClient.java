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
	
	private static boolean exit = false;
	
	public static void main(String[] args) {

		console = new H2HConsole();
		scanner = new Scanner(System.in);
		
		ConsoleMenu menu = new ConsoleMenu(console);
		menu.add("Login", new IConsoleMenuCallback() { public void invoke() { loginHandler(); }});
		menu.add("Exit", new IConsoleMenuCallback() { public void invoke() { exitHandler(); }});
		
		System.out.println("Welcome to the Hive2Hive console client!");
		
		while (!exit) {
			console.clear();
			System.out.println("Please select an option.");
			menu.show();
		}
		
		System.exit(0);
	}
	
	private static void loginHandler() {
		
		System.out.println("Selected Option: Login");
		scanner.nextLine();
		
		// TODO start the login process
	}
	
	private static void exitHandler() {
		
		System.out.println("Selected Option: Exit");
		exit = true;
	}
}