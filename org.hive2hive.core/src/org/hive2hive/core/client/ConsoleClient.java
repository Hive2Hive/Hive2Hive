package org.hive2hive.core.client;

import java.io.IOException;

import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	private static SessionInstance session;
	private static H2HConsole console;

	public static void main(String[] args) {

		console = new H2HConsole();
		session = new SessionInstance();

		try {
			H2HLoggerFactory.initFactory();
		} catch (IOException e){
			System.out.println("H2HLoggerFactory could not be initialized.");
		}

		System.out.println("Welcome to the Hive2Hive console client!\n");

		TopLevelMenu menu = new TopLevelMenu(console, session);
		menu.open();

		System.exit(0);
	}
}