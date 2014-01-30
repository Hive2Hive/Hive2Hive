package org.hive2hive.processes.implementations.share;

import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.security.EncryptionUtil;
import org.hive2hive.processes.framework.abstracts.ProcessStep;
import org.hive2hive.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.processes.implementations.context.ShareProcessContext;

/**
 * Generate a new domain key which will be used for storing the shared meta documents.
 * 
 * @author Seppi, Nico
 */
public class NewDomainKeyStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(NewDomainKeyStep.class);
	private final ShareProcessContext context;

	public NewDomainKeyStep(ShareProcessContext context) {
		this.context = context;
	}

	@Override
	protected void doExecute() throws InvalidProcessStateException {
		logger.debug(String.format("Generating new domain key fo sharing the folder '%s' with user '%s'.",
				context.getFolder().getName(), context.getFriendId()));

		KeyPair domainKey = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT);
		context.provideNewProtectionKeys(domainKey);
	}
}
