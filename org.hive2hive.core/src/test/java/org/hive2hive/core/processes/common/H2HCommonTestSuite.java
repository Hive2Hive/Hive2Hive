package org.hive2hive.core.processes.common;

import org.hive2hive.core.processes.common.base.BaseDirectMessageProcessStepTest;
import org.hive2hive.core.processes.common.base.BaseGetProcessStepTest;
import org.hive2hive.core.processes.common.base.BaseMessageProcessStepTest;
import org.hive2hive.core.processes.common.base.BasePutProcessStepTest;
import org.hive2hive.core.processes.common.base.BaseRemoveProcessStepTest;
import org.hive2hive.core.processes.login.GetUserProfileStepTest;
import org.hive2hive.core.processes.register.PutUserProfileStepTest;
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
