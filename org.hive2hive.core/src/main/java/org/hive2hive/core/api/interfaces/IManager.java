package org.hive2hive.core.api.interfaces;

/**
 * Interface for the managers
 * 
 * @author Christian, Nico, Seppi
 * 
 */
public interface IManager {

	/**
	 * Configures whether processes by this component get started automatically or not.
	 * 
	 * @param autostart
	 */
	void configureAutostart(boolean autostart);

	/**
	 * Returns the configured autostart state
	 * 
	 * @return true when the autostart is enabled, else false. Change the autostart setting using
	 *         {@link IManager#configureAutostart(boolean)}
	 */
	boolean isAutostart();

}
