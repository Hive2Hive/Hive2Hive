package org.hive2hive.processes.test.framework.abstracts;

import org.hive2hive.core.test.H2HJUnitTest;
import org.hive2hive.core.test.model.FileTreeNodeTest;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProcessComponentTest extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = FileTreeNodeTest.class;
		beforeClass();
	}
	
	@Test
	public void startTest() {
		
	}
	
	@Test
	public void pauseTest() {
		
	}
	
	@Test
	public void resumeTest() {
		
	}
	
	@Test
	public void cancelTest() {
		
	}
}
