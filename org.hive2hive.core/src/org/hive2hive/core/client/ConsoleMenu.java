package org.hive2hive.core.client;

import java.util.ArrayList;
import java.util.Scanner;

public abstract class ConsoleMenu {

	protected final H2HConsole console;
	private final ArrayList<ConsoleMenuItem> items;
	
	private boolean exited;

	public ConsoleMenu(H2HConsole console) {
		this.console = console;
		this.items = new ArrayList<ConsoleMenuItem>();
		this.exited = false;
		
		addMenuHandlers();
		
		add("Exit", new IConsoleMenuCallback() {
			
			@Override
			public void invoke() {
				System.out.println("Selected Option: Exit");
				exitHandler();				
			}
		});
	}
	
	protected final void add(String displayText, IConsoleMenuCallback callback) {
		items.add(new ConsoleMenuItem(displayText, callback));
	}
	
	protected abstract void addMenuHandlers();

	public void open() {
		
		while (!exited) {
			console.clear();
			System.out.println(getInstruction());
			show();
		}
	}

	private final void show() {
		
		for (int i = 0; i < items.size(); ++i) {
			ConsoleMenuItem item = items.get(i);
			System.out.print(String.format("    [%s] %s\n", i + 1, item.getDisplayText()));
		}
		System.out.println();
		
		Scanner input = new Scanner(System.in);
		int chosen = 0;
		try {
			chosen = input.nextInt();
		} catch (Exception e) {
		}
		
		if (chosen > items.size() || chosen < 1){
			System.out.println(String.format("Invalid option. Select an option from 1 to %s.", items.size()));
			input.nextLine();
		} else {
			ConsoleMenuItem item = items.get(chosen - 1);
			IConsoleMenuCallback callback = item.getCallback();
			callback.invoke();
		}
		input.close();
	}

	private void exitHandler() {
		exited = true;
	}
	
	protected void printMenuSelection(String selectedOption){
		System.out.println("Selected Option: " + selectedOption);
	}
	
	protected abstract String getInstruction();
}
