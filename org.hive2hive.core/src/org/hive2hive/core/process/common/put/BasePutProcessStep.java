package org.hive2hive.core.process.common.put;

import org.hive2hive.core.log.H2HLogger;
import org.hive2hive.core.log.H2HLoggerFactory;
import org.hive2hive.core.network.data.DataManager;
import org.hive2hive.core.network.data.NetworkContent;
import org.hive2hive.core.network.data.listener.IPutListener;
import org.hive2hive.core.network.data.listener.IRemoveListener;
import org.hive2hive.core.process.ProcessStep;

/**
 * A process step which puts a {@link NetworkContent} object under the given keys. </br>
 * <b>Important:</b> Use only this process step to put some data into the network so that in case of failure a
 * appropriate handling is triggered.
 * 
 * @author Seppi
 */
public abstract class BasePutProcessStep extends ProcessStep implements IPutListener, IRemoveListener {
	
	private static final H2HLogger logger = H2HLoggerFactory.getLogger(BasePutProcessStep.class);

	protected String locationKey;
	protected String contentKey;
	protected NetworkContent content;
	protected ProcessStep nextStep;

	public BasePutProcessStep(ProcessStep nextStep) {
		this.nextStep = nextStep;
	}

	protected void put(String locationKey, String contentKey, NetworkContent content) {
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.content = content;
		
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			getProcess().stop("Node is not connected.");
			return;
		}
		dataManager.putGlobal(locationKey, contentKey, content, this);
	}

	@Override
	public void onPutSuccess() {
		getProcess().setNextStep(nextStep);
	}

	@Override
	public void onPutFailure() {
		getProcess().stop("Put failed.");
	}
	
	@Override
	public void rollBack() {
		DataManager dataManager = getNetworkManager().getDataManager();
		if (dataManager == null) {
			logger.warn(String.format(
					"Roll back of put failed. No connection. location key = '%s' content key = '%s' version key = '%s'",
					locationKey, contentKey, content.getVersionKey()));
			getProcess().nextRollBackStep();
			return;
		}
		dataManager.remove(locationKey, contentKey, content.getVersionKey(), this);
	}
	
	@Override
	public void onRemoveSuccess() {
		logger.debug(String.format("Roll back of put succeeded. location key = '%s' content key = '%s' version key = '%s'",
				locationKey, contentKey, content.getVersionKey()));
		getProcess().nextRollBackStep();
	}

	@Override
	public void onRemoveFailure() {
		logger.warn(String.format(
				"Roll back of put failed. Remove failed. location key = '%s' content key = '%s' version key = '%s'",
				locationKey, contentKey, content.getVersionKey()));
		getProcess().nextRollBackStep();
	}
}
