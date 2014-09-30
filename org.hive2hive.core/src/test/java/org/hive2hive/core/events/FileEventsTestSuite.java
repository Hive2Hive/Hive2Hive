package org.hive2hive.core.events;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	FileAddEventsTest.class, 
	FileDeleteEventsTest.class,
	FileMoveEventsTest.class 
})

public class FileEventsTestSuite {

}
