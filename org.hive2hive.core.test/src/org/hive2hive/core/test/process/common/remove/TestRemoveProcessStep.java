package org.hive2hive.core.test.process.common.remove;

import org.hive2hive.core.process.common.remove.BaseRemoveProcessStep;
import org.hive2hive.core.test.H2HTestData;

/**
 * A simple remove process step used at {@link RemoveProcessStepTest}.
 * 
 * @author Seppi
 */
public class TestRemoveProcessStep extends BaseRemoveProcessStep {
	
	private final String locationKey;
	private final String contentKey;
	private final H2HTestData data;

	public TestRemoveProcessStep(String locationKey, String contentKey, H2HTestData data) {
		super(null);
		this.locationKey = locationKey;
		this.contentKey = contentKey;
		this.data = data;
	}

	@Override
	public void start() {
		remove(locationKey, contentKey, data);
	}

}
