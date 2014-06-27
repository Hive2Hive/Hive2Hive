package org.hive2hive.core;

import java.security.SecureRandom;
import java.util.Random;

import org.hive2hive.core.security.EncryptionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2HJUnitTest {
	private static final String START_STRING = "Start ";
	private static final String END_STRING = "End ";

	protected static Class<? extends H2HJUnitTest> testClass;
	protected static Logger logger;

	@Rule
	public TestName name = new TestName();

	@Before
	public void beforeMethod() {
		logger.info(createMessage(name.getMethodName(), true));
	}

	@After
	public void afterMethod() {
		logger.info(createMessage(name.getMethodName(), false));
	}

	public static final void beforeClass() throws Exception {
		logger = LoggerFactory.getLogger(testClass);
		printTestIdentifier(testClass.getName(), true);
	}

	protected static void printTestIdentifier(String name, boolean isStart) {
		String bar = createBars(name, isStart);
		if (isStart) {
			logger.debug("");
		}
		logger.debug(bar);
		logger.info(createMessage(name, isStart));
		logger.debug(bar);
		if (!isStart) {
			logger.debug("");
		}

	}

	private static String createMessage(String name, boolean isStart) {
		StringBuffer message = new StringBuffer("--- ");
		if (isStart) {
			message.append(START_STRING);
		} else {
			message.append(END_STRING);
		}
		message.append(name);
		message.append(" ---");
		return message.toString();
	}

	private static String createBars(String name, boolean isStart) {
		StringBuffer bar = new StringBuffer();
		bar.append("----");
		int mitPart = name.length();
		if (isStart) {
			mitPart += START_STRING.length();
		} else {
			mitPart += END_STRING.length();
		}
		for (int i = 0; i < mitPart; i++) {
			bar.append("-");
		}
		bar.append("----");
		return bar.toString();
	}

	public static void afterClass() {
		printTestIdentifier(testClass.getName(), false);
	}

	protected static byte[] generateRandomContent(int sizeInBytes) {
		SecureRandom random = new SecureRandom();
		byte[] content = new byte[random.nextInt(sizeInBytes)];
		random.nextBytes(content);
		return content;
	}

	protected static byte[] generateFixedContent(int sizeInBytes) {
		SecureRandom random = new SecureRandom();
		byte[] content = new byte[sizeInBytes];
		random.nextBytes(content);
		return content;
	}

	/**
	 * Returns a random string that with a specified maximal length of chars. The returned string has at least
	 * one char.
	 * 
	 * @return
	 */
	public static String generateRandomString(int maxLength) {
		Random random = new Random();
		char[] subset = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

		int length = 0;
		while (length < 1) {
			length = random.nextInt(maxLength);
		}
		char buf[] = new char[length];
		for (int i = 0; i < buf.length; i++) {
			int index = random.nextInt(subset.length);
			buf[i] = subset[index];
		}

		return new String(buf);
	}

	protected static void printBytes(String description, byte[] bytes) {
		logger.debug(description);
		logger.debug(EncryptionUtil.byteToHex(bytes));
	}

}
