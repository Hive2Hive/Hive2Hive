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

	private static boolean exit;
	private static boolean registerExit;
	

	public static void main(String[] args) {

		
		console = new H2HConsole();
		scanner = new Scanner(System.in);

		ConsoleMenu menu = new ConsoleMenu(console);
		exit = false;
		
		menu.add("Register", new IConsoleMenuCallback() {
			public void invoke() {
				registerHandler();
			}
		});
		menu.add("Login", new IConsoleMenuCallback() {
			public void invoke() {
				loginHandler();
			}
		});
		menu.add("Exit", new IConsoleMenuCallback() {
			public void invoke() {
				exitHandler();
			}
		});

		System.out.println("Welcome to the Hive2Hive console client!");

		while (!exit) {
			console.clear();
			System.out.println("Please select an option.");
			menu.show();
		}

		System.exit(0);
	}

	private static void registerHandler() {

		System.out.println("Selected Option: Register");

		ConsoleMenu registerMenu = new ConsoleMenu(console);
		registerExit = false;

		registerMenu.add("Register", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Set MaxFileSize", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Set MaxNumOfVersions", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Set MaxSizeAllVersions", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Set ChunkSize", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Set AutostartProcesses", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Set IsMasterPeer", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Set BootstrapAddress", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Set RootPath", new IConsoleMenuCallback() {
			public void invoke() {
				
			}
		});
		
		registerMenu.add("Back", new IConsoleMenuCallback() {
			public void invoke() {
				backHandler(registerExit);
			}
		});

		while (!registerExit) {
			console.clear();
			System.out.println("Please selec a register option.");
			registerMenu.show();
		}
	}
	
	private static void backHandler(boolean exit) {
		exit = true;
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