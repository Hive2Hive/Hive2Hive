package org.hive2hive.client.menu;

import java.text.SimpleDateFormat;

import org.hive2hive.client.console.ConsoleMenu;
import org.hive2hive.client.console.H2HConsoleMenuItem;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

public class LoggerMenu extends ConsoleMenu {

	@Override
	protected void addMenuItems() {

		add(new H2HConsoleMenuItem("Yes") {
			protected void execute() throws Exception {
				String logFileName = createRootLogger();
				print(String.format("Log file '%s' has been created.", logFileName));
				exit();
			}
		});
		add(new H2HConsoleMenuItem("No") {
			protected void execute() throws Exception {
				// logback.xml defines nothing by default
				LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
				loggerContext.reset();
				exit();
			}
		});
	}

	@Override
	protected String getInstruction() {
		return "Do you want this session to be logged?";
	}

	@Override
	protected String getExitItemText() {
		return "Cancel";
	}

	private static String createRootLogger() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		PatternLayoutEncoder ple = new PatternLayoutEncoder();

		ple.setPattern("%d{HH:mm:ss} %-12.-12([%thread])[%-5level] %logger{0} -%msg%n");
		ple.setContext(loggerContext);
		ple.start();

		String fileName = String.format("logs/h2h-log %s.txt",
				new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(System.currentTimeMillis()));
		FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
		fileAppender.setFile(fileName);
		fileAppender.setEncoder(ple);
		fileAppender.setContext(loggerContext);
		fileAppender.start();

		Logger logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		logbackLogger.addAppender(fileAppender);
		logbackLogger.setLevel(Level.DEBUG);
		logbackLogger.setAdditive(false);

		return fileName;
	}
}
