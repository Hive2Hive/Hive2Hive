package org.hive2hive.client.console;

import org.hive2hive.client.util.MenuContainer;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.concretes.ProcessComponentListener;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;

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

	protected boolean executeBlocking(IProcessComponent process, String itemName) throws InterruptedException,
			InvalidProcessStateException {

		print(String.format("Executing '%s'...", itemName));

		ProcessComponentListener listener = new ProcessComponentListener();

		process.attachListener(listener);
		process.start().await();

		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			print(String.format("The process has failed%s", reason != null ? ": " + reason.getHint() : "."));
		}
		return listener.hasSucceeded();
	}
}