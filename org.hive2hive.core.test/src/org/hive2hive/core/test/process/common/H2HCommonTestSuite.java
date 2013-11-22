package org.hive2hive.core.test.process.common;

import org.hive2hive.core.test.process.common.get.BaseGetProcessStepTest;
import org.hive2hive.core.test.process.common.get.GetLocationStepTest;
import org.hive2hive.core.test.process.common.get.GetUserProfileStepTest;
import org.hive2hive.core.test.process.common.massages.BaseDirectMessageProcessStepTest;
import org.hive2hive.core.test.process.common.massages.BaseMessageProcessStepTest;
import org.hive2hive.core.test.process.common.put.BasePutProcessStepTest;
import org.hive2hive.core.test.process.common.put.PutLocationStepTest;
import org.hive2hive.core.test.process.common.put.PutUserProfileStepTest;
import org.hive2hive.core.test.process.common.remove.BaseRemoveProcessStepTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This suit bundles all tests of the common package, which contains the generic process steps for put, get,
 * remove and messages.
 * 
 * @author Seppi
 */
@RunWith(Suite.class)
@SuiteClasses({

// ProcessStep, Common, Get
		BaseGetProcessStepTest.class, GetLocationStepTest.class, GetUserProfileStepTest.class,

		// ProcessStep, Common, Put
		BasePutProcessStepTest.class, PutLocationStepTest.class, PutUserProfileStepTest.class,

		// ProcessStep, Common, Remove
		BaseRemoveProcessStepTest.class,

		// ProcessStep, Common, Message
		BaseMessageProcessStepTest.class, BaseDirectMessageProcessStepTest.class,

})
public class H2HCommonTestSuite {

}
