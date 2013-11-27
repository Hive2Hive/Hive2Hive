package org.hive2hive.core.client;

import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleMenuItem {

	private String displayText;
	private IConsoleMenuCallback callback;
	
	private final ArrayList<ConsoleMenuItem> items;
	
	public ConsoleMenuItem(String displayText, IConsoleMenuCallback callback) {
		this.displayText = displayText;
		this.callback = callback;
		
		this.items = new ArrayList<ConsoleMenuItem>();
	}
	
	public boolean add(String displayText, IConsoleMenuCallback callback) {
		return items.add(new ConsoleMenuItem(displayText, callback));
	}
	
	public void show() {
		
		for (int i = 0; i < items.size(); ++i) {
			ConsoleMenuItem item = items.get(i);
			System.out.print(String.format("[%s] %s\n", i + 1, item.getDisplayText()));
		}
		System.out.println();
		
		Scanner input = new Scanner(System.in);
		int chosen = 0;
		if (chosen > items.size() || chosen < 1){
			System.out.println("Invalid option.\nPress enter to continue...");
			input.nextLine();
			input.nextLine();
		} else {
			ConsoleMenuItem item = items.get(chosen - 1);
			IConsoleMenuCallback callback = item.getCallback();
			callback.invoke();
		}
		input.close();
	}
	
	public String getDisplayText() {
		return displayText;
	}
	
	public IConsoleMenuCallback getCallback() {
		return callback;
	}
}
