package org.hive2hive.processframework.interfaces;

/**
 * Interface for process components that intend to deliver a result.
 * 
 * @author Christian
 * 
 * @param <T> The type of the result object.
 */
public interface IResultProcessComponent<T> extends IProcessComponent {

	/**
	 * Returns the computed result. Maybe <code>null</code> in case the computation has not finished yet.
	 * @return Result or <code>null</code> if computation not finished.
	 */
	T getResult();
	
	void notifyResultComputed(T result);
	
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