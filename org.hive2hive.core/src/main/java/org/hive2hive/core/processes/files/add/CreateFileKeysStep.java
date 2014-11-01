package org.hive2hive.core.processes.files.add;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.processes.context.AddFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.abstracts.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Create a new {@link KeyPair} which serves as id of the index and encryption key pair for the corresponding
 * meta file. Additionally the public key part is the location where the meta file gets stored.
 * 
 * @author Seppi
 */
public class CreateFileKeysStep extends ProcessStep {

	private final AddFileProcessContext context;

	public CreateFileKeysStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		// generate the meta keys used for encrypting the meta file and as id of the meta file and index
		KeyPair metaKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);

		context.provideFileKeys(metaKeys);
	}

	@Override
	protected void doRollback(RollbackReason reason) throws InvalidProcessStateException {
		context.provideFileKeys(null);
	}
}
