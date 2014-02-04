package org.hive2hive.core.processes.framework.interfaces;

import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;

public interface IControllable {

	void start() throws InvalidProcessStateException;

	void pause() throws InvalidProcessStateException;

	void resume() throws InvalidProcessStateException;

	void cancel(RollbackReason reason) throws InvalidProcessStateException;
}