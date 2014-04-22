package org.hive2hive.client.util;

import org.hive2hive.client.menu.FileMenu;
import org.hive2hive.client.menu.FileObserverMenu;
import org.hive2hive.client.menu.NodeMenu;
import org.hive2hive.client.menu.RootMenu;
import org.hive2hive.client.menu.UserMenu;

/**
 * Container for all console menus used by the console client.
 * @author Christian
 *
 */
public class MenuContainer {

	private final RootMenu rootMenu = new RootMenu(this);
	private final NodeMenu nodeMenu = new NodeMenu(this);
	private final UserMenu userMenu = new UserMenu(this);
	private final FileMenu fileMenu = new FileMenu(this);
	private final FileObserverMenu fileObserverMenu = new FileObserverMenu(this);
	
	public RootMenu getRootMenu() {
		return rootMenu;
	}

	public NodeMenu getNodeMenu() {
		return nodeMenu;
	}

	public UserMenu getUserMenu() {
		return userMenu;
	}

	public FileMenu getFileMenu() {
		return fileMenu;
	}

	public FileObserverMenu getFileObserverMenu() {
		return fileObserverMenu;
	}

}
