package org.hive2hive.core.processes.files.download.direct;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.common.GetUserLocationsStep;
import org.hive2hive.core.processes.context.interfaces.IGetUserLocationsContext;
import org.hive2hive.processframework.concretes.SequentialProcess;
import org.hive2hive.processframework.decorators.AsyncComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets a list of all locations (using the internal process framework
 * 
 * @author Nico
 */
public class GetLocationsList implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(GetLocationsList.class);

	private final DownloadTaskDirect task;
	private final IDataManager dataManager;

	public GetLocationsList(DownloadTaskDirect task, IDataManager dataManager) {
		this.task = task;
		this.dataManager = dataManager;
	}

	@Override
	public void run() {
		Set<Locations> collectingSet = Collections.newSetFromMap(new ConcurrentHashMap<Locations, Boolean>());

		SequentialProcess process = new SequentialProcess();
		for (String user : task.getUsers()) {
			ProvideUserLocationsContext context = new ProvideUserLocationsContext(collectingSet, user);
			GetUserLocationsStep step = new GetUserLocationsStep(context, dataManager);
			process.add(new AsyncComponent(step));
		}

		try {
			logger.debug("Started getting the list of locations to download {}", task.getDestinationName());
			process.start().await();
		} catch (InvalidProcessStateException | InterruptedException e) {
			task.provideLocations(new HashSet<Locations>());
			task.abortDownload(e.getMessage());
			return;
		}

		if (logger.isDebugEnabled()) {
			int numPeerAddresses = 0;
			for (Locations locations : collectingSet) {
				numPeerAddresses += locations.getPeerAddresses().size();
			}
			logger.debug("Got {} candidate location(s) with {} peer address(es) to download {}", collectingSet.size(),
					numPeerAddresses, task.getDestinationName());
		}

		task.provideLocations(collectingSet);
	}

	/**
	 * Local context for holding multiple locations
	 * 
	 * @author Nico
	 * 
	 */
	private class ProvideUserLocationsContext implements IGetUserLocationsContext {

		private final String userId;
		private final Set<Locations> allLocationsCollection;

		public ProvideUserLocationsContext(Set<Locations> allLocationsCollection, String userId) {
			this.allLocationsCollection = allLocationsCollection;
			this.userId = userId;
		}

		@Override
		public void provideUserLocations(Locations locations) {
			if (locations != null) {
				// forward the locations to the (thread-safe) super-collection
				logger.debug("User {} has {} peers online to ask", userId, locations.getPeerAddresses().size());
				allLocationsCollection.add(locations);
			}
		}

		@Override
		public String consumeUserId() {
			return userId;
		}
	}
}
