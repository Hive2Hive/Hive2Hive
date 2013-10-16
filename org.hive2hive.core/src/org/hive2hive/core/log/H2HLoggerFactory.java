package org.hive2hive.core.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.RootLogger;

public final class H2HLoggerFactory {
	private static final H2HLoggerFactory factory = new H2HLoggerFactory();
	private Hierarchy log4jHierarchy;
	private boolean isConfigured;

	private H2HLoggerFactory() {
	}

	public static void initFactory() throws IOException {
		if (!factory.isConfigured) {
			factory.configureLogging();
		}
	}

	public static H2HLogger getLogger(Class<?> aClass) {
		return (H2HLogger) factory.log4jHierarchy.getLogger(aClass.getName());
	}

	/**
	 * Configures logging
	 */
	private void configureLogging() throws IOException {

		InputStream propertiesInputStream = this.getClass().getResourceAsStream("config/log4j.properties");

		if (propertiesInputStream != null) {
			Properties props = new Properties();
			props.load(propertiesInputStream);
			log4jHierarchy = new H2HLogHierarchy(new RootLogger(Level.DEBUG));
			new PropertyConfigurator().doConfigure(props, log4jHierarchy);
		}

		propertiesInputStream.close();

		factory.isConfigured = true;
	}
}
