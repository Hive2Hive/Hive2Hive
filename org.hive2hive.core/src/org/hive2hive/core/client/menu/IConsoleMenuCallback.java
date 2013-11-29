package org.hive2hive.core.client.menu;

import java.util.EventListener;

/**
 * A menu callback interface for invoking methods.
 * 
 * @author Christian
 * 
 */
public interface IConsoleMenuCallback extends EventListener {

	public void invoke();
}
