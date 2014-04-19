package org.hive2hive.client.console;

import org.hive2hive.client.util.MenuContainer;
import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.concretes.ProcessComponentListener;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.framework.interfaces.IProcessComponent;

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

	protected void executeBlocking(IProcessComponent process, String itemName) throws InterruptedException,
			InvalidProcessStateException {

		print(String.format("Executing '%s'...", itemName));

		ProcessComponentListener listener = new ProcessComponentListener();

		process.attachListener(listener);
		process.start().await();

		if (listener.hasFailed()) {
			RollbackReason reason = listener.getRollbackReason();
			print(String.format("The process has failed%s", reason != null ? reason.getHint() : "."));
		}
	}
}