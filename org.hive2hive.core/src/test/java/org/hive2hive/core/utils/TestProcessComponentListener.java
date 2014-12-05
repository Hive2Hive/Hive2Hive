package org.hive2hive.core.utils;

import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;

public class TestProcessComponentListener implements IProcessComponentListener {

	protected boolean isExecuting;
	protected boolean isRollbacking;
	protected boolean isPaused;

	protected boolean hasExecutionSucceeded;
	protected boolean hasExecutionFailed;
	protected boolean hasRollbackSucceeded;
	protected boolean hasRollbackFailed;

	@Override
	public void onExecuting(IProcessEventArgs args) {
		isExecuting = true;
		isRollbacking = false;
		isPaused = false;
	}

	@Override
	public void onRollbacking(IProcessEventArgs args) {
		isExecuting = false;
		isRollbacking = true;
		isPaused = false;
	}

	@Override
	public void onPaused(IProcessEventArgs args) {
		isExecuting = false;
		isRollbacking = false;
		isPaused = true;
	}

	@Override
	public void onExecutionSucceeded(IProcessEventArgs args) {
		hasExecutionSucceeded = true;
	}

	@Override
	public void onExecutionFailed(IProcessEventArgs args) {
		hasExecutionFailed = true;
	}

	@Override
	public void onRollbackSucceeded(IProcessEventArgs args) {
		hasRollbackSucceeded = true;
	}

	@Override
	public void onRollbackFailed(IProcessEventArgs args) {
		hasRollbackFailed = true;
	}

	public boolean isExecuting() {
		return isExecuting;
	}

	public boolean isRollbacking() {
		return isRollbacking;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public boolean hasExecutionSucceeded() {
		return hasExecutionSucceeded;
	}

	public boolean hasExecutionFailed() {
		return hasExecutionFailed;
	}

	public boolean hasRollbackSucceeded() {
		return hasRollbackSucceeded;
	}

	public boolean hasRollbackFailed() {
		return hasRollbackFailed;
	}
}