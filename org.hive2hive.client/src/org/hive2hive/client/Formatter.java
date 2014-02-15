package org.hive2hive.client;

import static org.fusesource.jansi.Ansi.ansi;

import org.fusesource.jansi.Ansi.Color;

/**
 * A helper class to easily configure the console colors.
 * 
 * @author Christian
 * 
 */
public class Formatter {

	public static void setDefaultForeground() {

		if (isWindows()) {
			System.out.println(ansi().fg(Color.YELLOW));
		} else {
			System.out.println(ansi().fgBright(Color.YELLOW));
		}
	}
	
	public static void setInputForeground() {
		if (isWindows()) {
			System.out.println(ansi().fg(Color.WHITE));
		} else {
			System.out.println(ansi().fgBright(Color.WHITE));
		}
	}

	public static void setExecutionForeground() {
		if (isWindows()) {
			System.out.println(ansi().fg(Color.WHITE));
		} else {
			System.out.println(ansi().fgBright(Color.WHITE));
		}
	}

	public static void setSuccessForeground() {
		if (isWindows()) {
			System.out.println(ansi().fg(Color.GREEN));
		} else {
			System.out.println(ansi().fgBright(Color.GREEN));
		}
	}
	
	public static void setErrorForeground() {
		if (isWindows()) {
			System.out.println(ansi().fg(Color.RED));
		} else {
			System.out.println(ansi().fgBright(Color.RED));
		}
	}
	
	public static void setBackground(Color color) {
		System.out.println(ansi().bg(color));
	}

	public static void reset() {
		System.out.println(ansi().reset());
	}

	private static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}
}
