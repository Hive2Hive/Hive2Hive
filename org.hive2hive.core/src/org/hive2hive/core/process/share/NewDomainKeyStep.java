package org.hive2hive.core.process.share;

import java.io.File;
import java.security.KeyPair;

import org.apache.log4j.Logger;
import org.hive2hive.core.H2HConstants;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.process.ProcessStep;
import org.hive2hive.core.process.common.File2MetaFileStep;
import org.hive2hive.core.security.EncryptionUtil;

/**
 * Generate a new domain key which will be used for storing the shared meta documents.
 * 
 * @author Seppi
 */
public class NewDomainKeyStep extends ProcessStep {

	private final static Logger logger = H2HLoggerFactory.getLogger(NewDomainKeyStep.class);

	@Override
	public void start() {
		ShareFolderProcessContext context = (ShareFolderProcessContext) getProcess().getContext();
		logger.debug(String.format("Generating new domain key fo sharing the folder '%s' with user '%s'.",
				context.getFolderToShare().getName(), context.getFriendId()));

		KeyPair domainKey = EncryptionUtil.generateRSAKeyPair(H2HConstants.KEYLENGTH_META_DOCUMENT);
		context.setDomainKey(domainKey);

		File folderToShare = context.getFolderToShare();

		File2MetaFileStep file2MetaStep = new File2MetaFileStep(folderToShare, context.getSession()
				.getProfileManager(), context.getSession().getFileManager(), context,
				new UpdateMetaFolderStep());
		getProcess().setNextStep(file2MetaStep);
	}

	@Override
	public void rollBack() {
		// nothing to do here
		getProcess().nextRollBackStep();
	}

}
