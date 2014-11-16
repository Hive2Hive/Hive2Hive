package org.hive2hive.client.console;

import java.util.concurrent.ExecutionException;

import org.hive2hive.client.util.MenuContainer;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;

public abstract class H2HConsoleMenu extends ConsoleMenu {

	protected final MenuContainer menus;
	protected boolean isExpertMode;

	public H2HConsoleMenu(MenuContainer menus) {
		this.menus = menus;
	}

	public void open(boolean isExpertMode) {
		this.isExpertMode = isExpertMode;
		open();
	}

	public void reset() {
		// do nothing by default
	}

	public static <T> T executeBlocking(AsyncComponent<T> process, String itemName) throws InterruptedException,
			InvalidProcessStateException, ProcessExecutionException, ExecutionException {
		print(String.format("Executing '%s'...", itemName));
		return process.execute().get();
	}
}