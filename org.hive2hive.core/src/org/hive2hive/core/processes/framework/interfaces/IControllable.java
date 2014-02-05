package org.hive2hive.core.processes.framework.interfaces;

import java.util.concurrent.Future;

import org.hive2hive.core.processes.framework.RollbackReason;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;

public interface IControllable {

	Future<Boolean> start() throws InvalidProcessStateException;

	void pause() throws InvalidProcessStateException;

	void resume() throws InvalidProcessStateException;

	void cancel(RollbackReason reason) throws InvalidProcessStateException;
}