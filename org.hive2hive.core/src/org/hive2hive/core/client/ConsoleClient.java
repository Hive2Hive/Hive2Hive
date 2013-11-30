package org.hive2hive.core.client;

import java.awt.Color;
import java.io.IOException;

import org.hive2hive.core.client.console.Console;
import org.hive2hive.core.client.menu.TopLevelMenu;
import org.hive2hive.core.log.H2HLoggerFactory;

/**
 * A console-based client to use the Hive2Hive library.
 * 
 * @author Christian
 * 
 */
public class ConsoleClient {

	private static SessionInstance session;
	private static Console console;

	public static void main(String[] args) {

		console = new Console("Hive2Hive Console");
		session = new SessionInstance();

		try {
			H2HLoggerFactory.initFactory();
		} catch (IOException e){
			System.out.println("H2HLoggerFactory could not be initialized.");
		}

		System.out.println("Welcome to the Hive2Hive console client!\n");
		System.out.println("Configure your H2H network and nodes by browsing through the menus and follow the guides.\n");

		new TopLevelMenu(console, session).open();

		System.exit(0);
	}
}