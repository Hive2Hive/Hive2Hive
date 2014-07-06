package org.hive2hive.processframework.interfaces;

import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Separate controlling interface used by the process framework to allow components to be started, paused,
 * resumed and cancelled.
 * 
 * @author Christian
 * 
 */
public interface IControllable {

	/**
	 * Starts the component and therefore triggers its execution.
	 * 
	 * @throws InvalidProcessStateException If the component is in an invalid state for this operation.
	 */
	IProcessComponent start() throws InvalidProcessStateException;

	/**
	 * Pauses the execution or rollbacking of the component, depending on its current state.
	 * 
	 * @throws InvalidProcessStateException If the component is in an invalid state for this operation.
	 */
	void pause() throws InvalidProcessStateException;

	/**
	 * Resumes the execution or rollbacking of the component, depending on its current state.
	 * 
	 * @throws InvalidProcessStateException If the component is in an invalid state for this operation.
	 */
	void resume() throws InvalidProcessStateException;

	/**
	 * Cancels the component and therefore triggers its rollback.
	 * 
	 * @param reason The reason of the cancellation or fail.
	 * @throws InvalidProcessStateException If the component is in an invalid state for this operation.
	 */
	void cancel(RollbackReason reason) throws InvalidProcessStateException;
}