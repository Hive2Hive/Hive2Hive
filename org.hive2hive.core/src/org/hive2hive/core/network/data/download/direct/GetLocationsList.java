package org.hive2hive.core.network.data.download.direct;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hive2hive.core.model.Locations;
import org.hive2hive.core.network.data.IDataManager;
import org.hive2hive.core.processes.framework.concretes.SequentialProcess;
import org.hive2hive.core.processes.framework.decorators.AsyncComponent;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.implementations.common.GetUserLocationsStep;
import org.hive2hive.core.processes.implementations.context.interfaces.IProvideLocations;

/**
 * Gets a list of all locations (using the internal process framework
 * 
 * @author Nico
 * 
 */
public class GetLocationsList implements Runnable {

	private final DownloadTaskDirect task;
	private final Set<String> users;
	private final IDataManager dataManager;

	public GetLocationsList(DownloadTaskDirect task, Set<String> users, IDataManager dataManager) {
		this.task = task;
		this.users = users;
		this.dataManager = dataManager;
	}

	@Override
	public void run() {
		ProvideUserLocationsContext context = new ProvideUserLocationsContext();

		SequentialProcess process = new SequentialProcess();
		for (String user : users) {
			GetUserLocationsStep step = new GetUserLocationsStep(user, context, dataManager);
			process.add(new AsyncComponent(step));
		}

		try {
			process.start().await();
		} catch (InvalidProcessStateException | InterruptedException e) {
			task.abortDownload(e.getMessage());
			return;
		}

		task.provideLocations(context.getLocations());
	}

	private class ProvideUserLocationsContext implements IProvideLocations {

		private Set<Locations> locations;

		public ProvideUserLocationsContext() {
			this.locations = Collections.newSetFromMap(new ConcurrentHashMap<Locations, Boolean>());
		}

		@Override
		public void provideLocations(Locations locations) {
			this.locations.add(locations);
		}

		public Set<Locations> getLocations() {
			return locations;
		}
	}
}
