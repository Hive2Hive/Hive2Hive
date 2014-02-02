package org.hive2hive.processes.framework.abstracts;

import java.util.List;

import org.hive2hive.processes.framework.ProcessState;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;

public abstract class ProcessDecorator extends ProcessComponent {

	protected final ProcessComponent decoratedComponent;

	private boolean componentSucceeded = false;
	private boolean componentFailed = false;

	public ProcessDecorator(ProcessComponent decoratedComponent) {
		this.decoratedComponent = decoratedComponent;

		this.decoratedComponent.attachListener(new IProcessComponentListener() {

			@Override
			public void onSucceeded() {
				componentSucceeded = true;
				succeed();
			}

			@Override
			public void onFailed(RollbackReason reason) {
				componentFailed = true;
				fail(reason);
				// try {
				// cancel(reason);
				// } catch (InvalidProcessStateException e) {
				// e.printStackTrace();
				// }
			}

			@Override
			public void onFinished() {
				// ignore
			}
		});

	}

	@Override
	protected void succeed() {

		// decorator succeeds not until component succeeds
		if (componentSucceeded) {
			super.succeed();
		}
	}

	@Override
	protected void fail(RollbackReason reason) {

		// decorator fails not until component succeeds
		if (componentFailed) {
			super.fail(reason);
		}
	}

	@Override
	public void attachListener(IProcessComponentListener listener) {
		decoratedComponent.attachListener(listener);

		// Note: in case a decorator wants to attach listeners, it can do so by overriding this method. But
		// then, the current usage (whether attached to decorator or component directly) must be checked (and
		// corrected).
	}

	@Override
	public void detachListener(IProcessComponentListener listener) {
		decoratedComponent.attachListener(listener);
	}

	@Override
	public List<IProcessComponentListener> getListener() {
		return decoratedComponent.getListener();
	}

	@Override
	public String getID() {
		return decoratedComponent.getID();
	}

	@Override
	public double getProgress() {
		return decoratedComponent.getProgress();
	}

	@Override
	public ProcessState getState() {
		return decoratedComponent.getState();
	}

	@Override
	public void setParent(Process parent) {
		decoratedComponent.setParent(parent);
	}

	@Override
	public Process getParent() {
		return decoratedComponent.getParent();
	}

}
