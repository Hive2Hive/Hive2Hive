package org.hive2hive.client.menu;

import java.util.List;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.SelectionMenuItem;

public class SelectionMenu<T> extends ConsoleMenu {

	private final List<T> options;
	private final List<String> displayTexts;
	private final String instructions;
	private T selection = null;

	SelectionMenu(List<T> options, List<String> displayTexts, String instructions) {
		this.options = options;
		this.displayTexts = displayTexts;
		this.instructions = instructions;
	}

	SelectionMenu(List<T> options, String instructions) {
		this(options, null, instructions);
	}

	@Override
	protected void addMenuItems() {
		for (int i = 0; i < options.size(); i++) {
			add(new SelectionMenuItem<T>(options.get(i),
					displayTexts != null && i < displayTexts.size() ? displayTexts.get(i) : options.get(i)
							.toString()) {
				protected void execute() throws Exception {
					selection = option;
					exit();
				}
			});
		}
	}

	public T openAndSelect() {
		open();

		return selection;
	}

	@Override
	protected String getInstruction() {
		return instructions;
	}

}
