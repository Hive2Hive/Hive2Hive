package org.hive2hive.core.test;

import org.hive2hive.core.test.flowcontrol.ProcessManagerTest;
import org.hive2hive.core.test.tomp2p.ReplicationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This suit bundles all tests of hive2hive.
 * 
 * @author Seppi
 */
@RunWith(Suite.class)
@SuiteClasses({
// All tests of Hive2Hive

		// TomP2P
		ReplicationTest.class,

		// Process Manager
		ProcessManagerTest.class

})
public class H2HTestSuite {

}
