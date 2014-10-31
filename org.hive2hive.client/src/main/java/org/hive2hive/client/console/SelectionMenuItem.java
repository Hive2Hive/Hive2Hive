package org.hive2hive.client.console;

public abstract class SelectionMenuItem<T> extends H2HConsoleMenuItem {

	protected final T option;

	public SelectionMenuItem(T option, String displayText) {
		super(displayText);
		this.option = option;
	}
}
