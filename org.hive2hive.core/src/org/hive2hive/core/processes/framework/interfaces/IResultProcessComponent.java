package org.hive2hive.core.processes.framework.interfaces;

/**
 * Interface for process components that intend to deliver a result.
 * 
 * @author Christian
 * 
 * @param <T> The type of the result object.
 */
public interface IResultProcessComponent<T> extends IProcessComponent {

	/**
	 * Attaches an {@link IProcessResultListener} to the process component.
	 * 
	 * @param listener The listener to be attached.
	 */
	void attachListener(IProcessResultListener<T> listener);

	/**
	 * Detaches an {@link IProcessResultListener} from the process component.
	 * 
	 * @param listener The listener to be detached.
	 */
	void detachListener(IProcessResultListener<T> listener);

}