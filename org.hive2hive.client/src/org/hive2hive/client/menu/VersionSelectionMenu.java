package org.hive2hive.client.menu;

import java.util.List;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.VersionMenuItem;
import org.hive2hive.core.model.IFileVersion;

public class VersionSelectionMenu extends ConsoleMenu {

	// TODO implement a generic version of value-returning console menus
	
	private final List<IFileVersion> fileVersions;
	private IFileVersion selection = null;

	public VersionSelectionMenu(List<IFileVersion> fileVersions) {
		this.fileVersions = fileVersions;
	}
	
	@Override
	protected void addMenuItems() {
		for (int i = 0; i < fileVersions.size(); i++) {
			add(new VersionMenuItem(fileVersions.get(i)) {
				protected void execute() throws Exception {
					selection = fileVersion;
					exit();
				}
			});
		}
	}

	public IFileVersion openAndSelect() {
		open();
		
		return selection;
	}
	
	@Override
	protected String getInstruction() {
		return "Choose the version you want to recover.";
	}

}
