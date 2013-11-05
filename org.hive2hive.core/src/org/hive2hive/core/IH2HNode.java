package org.hive2hive.core;

import org.hive2hive.core.process.Process;

public interface IH2HNode {

	Process register(String userId, String password, String pin);

	Process login(String userId, String password, String pin);
}
