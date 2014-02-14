package org.hive2hive.core.api.interfaces;

public interface IManager {

	/**
	 * Configures whether processes by this component get started automatically or not.
	 * 
	 * @param autostart
	 */
	void configureAutostart(boolean autostart);

	boolean isAutostart();

}
