package org.hive2hive.core.test.tomp2p;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

import org.hive2hive.core.test.H2HJUnitTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TimestampTest  extends H2HJUnitTest {

	@BeforeClass
	public static void initTest() throws Exception {
		testClass = TimestampTest.class;
		beforeClass();
	}
	
	@Test
	public void testTimeStampTest1() {
		long timestamp = new Date().getTime();
		String content = "some content";
		Data data = new Data(content.getBytes());
		Number160 versionKey = new Number160(timestamp, data.hash());
		
		assertEquals(timestamp, versionKey.timestamp());
	}

	/*
	 * The assumption in this test might be wrong.
	 */
	@Test
	public void testTimeStampTest2() {
		long timestamp = new Date().getTime();
		Number160 versionKey = new Number160(timestamp);
	
		assertEquals(timestamp, versionKey.timestamp());
	}
	
	@Test
	public void testTimeStampTest3() {
		long timestamp = new Date().getTime();

		Number160 versionKey1 = new Number160(timestamp, Number160.createHash("bla"));
		Number160 versionKey2 = new Number160(timestamp, Number160.createHash("blub"));
	
		assertEquals(versionKey1.timestamp(), versionKey2.timestamp());
	}
	
	@Test
	public void testTimeStampTest4() {
		long timestamp = new Date().getTime();

		Number160 versionKey1 = new Number160(timestamp, Number160.ZERO);
		Number160 versionKey2 = new Number160(timestamp, Number160.ONE);
	
		assertEquals(versionKey1.timestamp(), versionKey2.timestamp());
	}
		
	@AfterClass
	public static void cleanAfterClass() {
		afterClass();
	}

}
