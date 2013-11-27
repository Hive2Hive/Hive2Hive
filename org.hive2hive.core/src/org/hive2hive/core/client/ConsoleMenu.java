package org.hive2hive.core.client;

import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleMenu {

	private final H2HConsole console;
	private final ArrayList<ConsoleMenuItem> items;
	
	public ConsoleMenu(H2HConsole console) {
		this.console = console;
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
		try {
			chosen = input.nextInt();
		} catch (Exception e) {
		}
		
		if (chosen > items.size() || chosen < 1){
			System.out.println("Invalid option. Press enter to continue...");
			input.nextLine();
		} else {
			ConsoleMenuItem item = items.get(chosen - 1);
			IConsoleMenuCallback callback = item.getCallback();
			callback.invoke();
		}
		input.close();
	}
}
