package org.hive2hive.client.menu;

import java.util.List;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.hive2hive.core.model.IFileVersion;

public class VersionSelectionMenu extends ConsoleMenu {

	// TODO implement a generic version of value-returning console menus
	
	private final List<IFileVersion> fileVersions;
	private IFileVersion selection;

	public VersionSelectionMenu(List<IFileVersion> fileVersions) {
		this.fileVersions = fileVersions;
	}
	
//	public final IFileVersion open() {
//		addMenuItems();
//		addExitItem();
//
//		this.exited = false;
//		while (!exited) {
//			show();
//		}
//		onMenuExit();
//	}
	
	private final void exit(IFileVersion selection) {
		this.selection = selection;
	}
	
	@Override
	protected void addMenuItems() {
		for (int i = 0; i < fileVersions.size(); i++)
			add(new H2HConsoleMenuItem(fileVersions.get(i).toString()) {
				protected void execute() throws Exception {
					// TODO Auto-generated method stub
					
				}
			});
	}

	@Override
	protected String getInstruction() {
		// TODO Auto-generated method stub
		return null;
	}

}
