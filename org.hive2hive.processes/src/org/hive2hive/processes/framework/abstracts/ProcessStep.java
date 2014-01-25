package org.hive2hive.processes.framework.abstracts;

import java.security.PublicKey;

import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.processes.framework.RollbackReason;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;

public abstract class ProcessStep extends ProcessComponent {

	@Override
	public final void join() {
		// TODO Auto-generated method stub

	}

	@Override
	protected final void doPause() {
		// TODO Auto-generated method stub

	}

	@Override
	protected final void doResumeExecution() throws InvalidProcessStateException {
		// TODO Auto-generated method stub

	}

	@Override
	protected final void doResumeRollback() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		// do nothing by default
	}

	// TODO Nico: this method should not be here!
	protected String key2String(PublicKey key) {
		return EncryptionUtil.byteToHex(key.getEncoded());
	}

}
