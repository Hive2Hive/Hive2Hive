package org.hive2hive.core.processes.framework;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hive2hive.core.H2HJUnitTest;
import org.hive2hive.core.processes.framework.exceptions.InvalidProcessStateException;
import org.hive2hive.core.processes.util.TestProcessComponentListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcessListenerTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = ProcessListenerTest.class;
		beforeClass();
	}

	@AfterClass
	public static void endTest() {
		afterClass();
	}
	
	@Test
	public void listenerStateTest() throws InvalidProcessStateException {
		
		TestProcessComponentListener listener = new TestProcessComponentListener();
		assertFalse(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertFalse(listener.hasFinished());
		
		listener.onSucceeded();
		assertTrue(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertTrue(listener.hasFinished());
		listener.reset();
		
		listener.onFailed(new RollbackReason("Test fail."));
		assertFalse(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());
		listener.reset();
	}
	
	@Test
	public void listenerResetTest() {
		
		TestProcessComponentListener listener = new TestProcessComponentListener();
		assertFalse(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertFalse(listener.hasFinished());
		
		listener.onSucceeded();
		listener.onFailed(new RollbackReason("Test fail."));
		assertTrue(listener.hasSucceeded());
		assertTrue(listener.hasFailed());
		assertTrue(listener.hasFinished());
		
		listener.reset();
		assertFalse(listener.hasSucceeded());
		assertFalse(listener.hasFailed());
		assertFalse(listener.hasFinished());
	}
}
