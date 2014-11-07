package org.hive2hive.core.processes.files.add;

import java.security.KeyPair;

import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.processes.context.AddFileProcessContext;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.processframework.ProcessStep;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;

/**
 * Create a new {@link KeyPair} which serves as id of the index and encryption key pair for the corresponding
 * meta file. Additionally the public key part is the location where the meta file gets stored.
 * 
 * @author Seppi
 */
public class CreateFileKeysStep extends ProcessStep<Void> {

	private final AddFileProcessContext context;

	public CreateFileKeysStep(AddFileProcessContext context) {
		this.context = context;
	}

	@Override
	protected Void doExecute() throws InvalidProcessStateException {
		// generate the meta keys used for encrypting the meta file and as id of the meta file and index
		KeyPair metaKeys = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_FILE);

		context.provideFileKeys(metaKeys);
		setRequiresRollback(true);
		return null;
	}

	@Override
	protected Void doRollback() throws InvalidProcessStateException {
		context.provideFileKeys(null);
		setRequiresRollback(false);
		return null;
	}
}
