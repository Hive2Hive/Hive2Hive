package org.hive2hive.processes.test.framework;

import org.hive2hive.processes.framework.concretes.ProcessListener;
import org.hive2hive.processes.framework.interfaces.IProcessComponentListener;
import org.junit.Test;

public class ProcessListenerTest {

	@Test
	public void syncProcessListenerTest() {
		
		IProcessComponentListener listener = new ProcessListener();
	}
	
	@Test
	public void asyncProcessListenerTest() {
		
	}
}
