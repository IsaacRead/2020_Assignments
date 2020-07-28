package test.ac.vuw.swen301.a1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.pattern.LogEvent;
import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import nz.ac.vuw.swen301.a1.*;

public class MemAppenderTest {

	public static Logger createMemAppenderLogger(String logName, String appenName) {
		Logger logger = Logger.getLogger(logName);
		// logger.removeAllAppenders();
		MemAppender memAppender = new MemAppender();
		memAppender.setName(appenName);
		logger.addAppender(memAppender);
		try {
		    ObjectName objectName = new ObjectName("test.ac.vuw.swen301.a1:name="+ appenName);
		    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		    server.registerMBean(memAppender, objectName);
		} catch (Exception e) {
		   e.printStackTrace();
		   Assert.fail("mBean registoring failed");
		}
		return logger;
	}

//	public static void checkLogisOK(LoggingEvent log, String exptLoggerName, Level exptLevel, String exptMessage,
//			long exptTime, String exptThread) {
//	}

	@Test
	public void add_logs_test() {
		Logger logger = createMemAppenderLogger("test1Logger", "test1Appender");
		logger.trace("tracemessage");
		logger.debug("debugmessage");
		logger.info("infomessage");
		logger.warn("warnmessage");
		logger.error("errormessage");
		logger.fatal("fatalmessage");

		MemAppender memApp = (MemAppender) logger.getAppender("test1Appender");
		Assert.assertEquals("mem appender's log list was not the expected size", 6, memApp.getCurrentLogs().size());
	}

	@Test
	public void change_name() {
		Logger logger = createMemAppenderLogger("nametestLogger", "appInitName");
		MemAppender appender = (MemAppender) logger.getAppender("appInitName");
		Assert.assertNotNull("no appender was found with this name", appender);

		appender.setName("appNewName");
		appender = (MemAppender) logger.getAppender("appInitName");
		Assert.assertNull("appender with outdated name was found", appender);

		appender = (MemAppender) logger.getAppender("appNewName");
		Assert.assertNotNull("no appender was found with this name", appender);
	}

	@Test
	public void getCurrentLogs_natural_logs_1() {
		Logger logger = createMemAppenderLogger("logger1", "memAppender");
		logger.debug("debug message");
		long exptTime = Instant.now().toEpochMilli();
		MemAppender app = (MemAppender) logger.getAppender("memAppender");
		List<LoggingEvent> logs = app.getCurrentLogs();
		LoggingEvent log = logs.get(0);
		Assert.assertEquals("logged time was incorrect", exptTime, log.timeStamp, 40);
		Assert.assertEquals("logger Name was incorrect", "logger1", log.getLoggerName());
		Assert.assertEquals("logged level was inccorect", Level.DEBUG, log.getLevel());
		Assert.assertEquals("logged Thread was incorrect", Thread.currentThread().getName(), log.getThreadName());
	}

	@Test
	public void getCurrentLogs_artifical_logs_1() {
		Logger loggerMemApp = createMemAppenderLogger("logArt1", "appArt1");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appArt1");
		LoggingEvent[] exptAddedLogs = add_artificial_logs(loggerMemApp, app, 6);
		check_arr_equals_logs(exptAddedLogs, app, 6);
	}

	@Test
	public void getCurrentLogs_artifical_logs_2() {
		for (int i = 1; i < 999; i *= 3) {
			Logger loggerMemApp = createMemAppenderLogger(String.format("l %d", i), String.format("a %d", i));
			MemAppender app = (MemAppender) loggerMemApp.getAppender(String.format("a %d", i));
			LoggingEvent[] exptAddedLogs = add_artificial_logs(loggerMemApp, app, i);
			check_arr_equals_logs(exptAddedLogs, app, i);
		}
	}

	public LoggingEvent[] add_artificial_logs(Logger loggerMemApp, MemAppender app, int numOfLogs) {
		// Logger loggerMemApp = createMemAppenderLogger(loggerName, memAppenderName);

		LoggingEvent[] logArr = new LoggingEvent[numOfLogs];

		for (int i = 0; i < numOfLogs; i++) {
			Throwable thrw = null;
			if (i % 7 == 0) {
				thrw = new Throwable(String.format("throw %d", i));
			}

			switch (i % 6) {
			case 0:
				logArr[i] = new LoggingEvent(String.valueOf(i), loggerMemApp, i * 2, Level.TRACE,
						String.format("logmessage %d", i), thrw);
				break;
			case 1:
				logArr[i] = new LoggingEvent(String.valueOf(i), loggerMemApp, i * 20, Level.DEBUG,
						String.format("logmessage %d", i), thrw);
				break;
			case 2:
				logArr[i] = new LoggingEvent(String.valueOf(i), loggerMemApp, i * 200, Level.INFO,
						String.format("logmessage %d", i), thrw);
				break;
			case 3:
				logArr[i] = new LoggingEvent(String.valueOf(i), loggerMemApp, i * 2000, Level.WARN,
						String.format("logmessage %d", i), thrw);

				break;
			case 4:
				logArr[i] = new LoggingEvent(String.valueOf(i), loggerMemApp, i * 2222, Level.ERROR,
						String.format("logmessage %d", i), thrw);
				break;
			case 5:
				logArr[i] = new LoggingEvent(String.valueOf(i), loggerMemApp, i * 3333, Level.FATAL,
						String.format("logmessage %d", i), thrw);
				break;

			default:
				Assert.fail("test programming error");
				break;
			}
		}
		for (int i = 0; i < logArr.length; i++) {
			app.doAppend(logArr[i]);
		}
		return logArr;
	}

	public void check_arr_equals_logs(LoggingEvent[] exptLogArr, MemAppender app, long exptNumLogs) {
		List<LoggingEvent> memAppLogs = app.getCurrentLogs();
		Assert.assertEquals("Number of logs in mem appender is incorrect", exptNumLogs, memAppLogs.size());
		for (int i = 0; i < memAppLogs.size(); i++) {
			Assert.assertEquals("log in memAppender is inccorect", exptLogArr[i], memAppLogs.get(i));
		}
	}

	@Test
	public void getCurrentLogs_is_uncastable() {
		Logger loggerMemApp = createMemAppenderLogger("loggerUncast1", "appcast1");
		loggerMemApp.debug("message");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appcast1");

		Assert.assertThrows("Memappender logs list could be cast to linked list", ClassCastException.class, () -> {
			LinkedList<LoggingEvent> memAppLogs = (LinkedList<LoggingEvent>) app.getCurrentLogs();
		});
	}

	@Test
	public void getCurrentLogs_is_unaddable() {
		Logger loggerMemApp = createMemAppenderLogger("loggerUnadd1", "appUnadd1");
		loggerMemApp.debug("message");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appUnadd1");
		List<LoggingEvent> memAppLogs1 = app.getCurrentLogs();
		Assert.assertThrows("memAppenders log list could be added to", UnsupportedOperationException.class, () -> {
			memAppLogs1.add(new LoggingEvent(null, loggerMemApp, 100, Level.TRACE, String.format("logmessage"), null));
		});
	}

	@Test
	public void getCurrentLogs_is_unremovable() {
		Logger loggerMemApp = createMemAppenderLogger("loggerUnrem1", "appUnrem1");
		loggerMemApp.debug("message");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appUnrem1");
		List<LoggingEvent> memAppLogs1 = app.getCurrentLogs();
		Assert.assertThrows("memAppenders log list could be removed from", UnsupportedOperationException.class, () -> {
			memAppLogs1.remove(0);
		});
	}

	@Test
	public void maxSize_is_maintained() {
		Logger loggerMemApp = createMemAppenderLogger("loggermaxsize1", "appmaxsize1");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appmaxsize1");
		Assert.assertTrue("max size was not changed", app.setMaxSize(3));
		loggerMemApp.debug("message");
		loggerMemApp.debug("message");
		loggerMemApp.debug("message");
		Assert.assertEquals("number of logs added was not correct", 3, app.getCurrentLogs().size());
		loggerMemApp.debug("message");
		Assert.assertEquals("number of logs was not maintained to max size", 3, app.getCurrentLogs().size());
	}

	@Test
	public void maxSize_cant_be_negative() {
		Logger loggerMemApp = createMemAppenderLogger("loggermaxsize2", "appmaxsize2");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appmaxsize2");
		Assert.assertTrue("max size was not changed", app.setMaxSize(3));
		Assert.assertFalse("max size was changed to negative", app.setMaxSize(-1));
		Assert.assertEquals("max size is not 3", 3, app.getMaxSize());

	}

	// Test MemAppender removes appropriate amount of logs, oldest first
	// When max size is set AFTER logs are added
	@Test
	public void maxSize_removes_oldest_1() {
		Logger loggerMemApp = createMemAppenderLogger("loggerRemOldest", "appRemOldest");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appRemOldest");
		LoggingEvent[] exptAddedLogs = add_artificial_logs(loggerMemApp, app, 6);
		app.setMaxSize(5);
		Assert.assertEquals("Max size was not set", 5, app.getMaxSize());
		LinkedList<LoggingEvent> exptLogsList = new LinkedList<LoggingEvent>(Arrays.asList(exptAddedLogs));
		exptLogsList.remove(0);
		exptAddedLogs = exptLogsList.toArray(new LoggingEvent[0]);
		check_arr_equals_logs(exptAddedLogs, app, 5);
	}

	// Test MemAppender removes appropriate amount of logs, oldest first
	// When max size is set BEFORE logs are added
	@Test
	public void maxSize_removes_oldest_2() {
		Logger loggerMemApp = createMemAppenderLogger("loggerRemOldest2", "appRemOldest2");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appRemOldest2");
		app.setMaxSize(5);
		LoggingEvent[] exptAddedLogs = add_artificial_logs(loggerMemApp, app, 6);
		Assert.assertEquals("Max size was not set", 5, app.getMaxSize());
		LinkedList<LoggingEvent> exptLogsList = new LinkedList<LoggingEvent>(Arrays.asList(exptAddedLogs));
		exptLogsList.remove(0);
		exptAddedLogs = exptLogsList.toArray(new LoggingEvent[0]);
		check_arr_equals_logs(exptAddedLogs, app, 5);
	}

	// Test MemAppender removes appropriate amount of logs, oldest first
	// When max size is set DEFUALT of 1000
	@Test
	public void maxSize_removes_oldest_3() {
		Logger loggerMemApp = createMemAppenderLogger("loggerRemOldest3", "appRemOldest3");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appRemOldest3");
		LoggingEvent[] exptAddedLogs = add_artificial_logs(loggerMemApp, app, 1001);
		Assert.assertEquals("Max size default is not correct", 1000, app.getMaxSize());
		LinkedList<LoggingEvent> exptLogsList = new LinkedList<LoggingEvent>(Arrays.asList(exptAddedLogs));
		exptLogsList.remove(0);
		exptAddedLogs = exptLogsList.toArray(new LoggingEvent[0]);
		check_arr_equals_logs(exptAddedLogs, app, 1000);
	}

	@Test
	public void getDiscarded_is_accurate() {
		Logger loggerMemApp = createMemAppenderLogger("loggerGtDisc", "appGtdisc");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appGtdisc");
		add_artificial_logs(loggerMemApp, app, 6);
		app.setMaxSize(2);
		Assert.assertEquals("Max size was not set", 2, app.getMaxSize());
		Assert.assertEquals("", 4, app.getDiscardedLogCount());
	}

	@Test
	public void export_to_JSON_accurate() {
		Logger loggerMemApp = createMemAppenderLogger("loggerJSON1", "appJSON1");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appJSON1");
		LoggingEvent[] exptAddedLogs = add_artificial_logs(loggerMemApp, app, 6);
		try {
			app.exportToJSON("exportTest1.json");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		File file = new File("exportTest1.json");
		String jsonData = testUtils.readFile(file);
		JSONArray jArr = new JSONArray(jsonData);
		for (int i = 0; i < exptAddedLogs.length; i++) {
			testUtils.checkJSONMatch(jArr.getJSONObject(i), exptAddedLogs[i].getLevel().toString(), exptAddedLogs[i].getLoggerName(), exptAddedLogs[i].getTimeStamp(), exptAddedLogs[i].getThreadName(),
					 exptAddedLogs[i].getRenderedMessage());
		}
	}
	
	@Test
	public void getLogs_returns_accurate_string_array() {
		Logger loggerMemApp = createMemAppenderLogger("loggerGetLogs1", "appGetLogs1");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appGetLogs1");
		LoggingEvent[] exptAddedLogs = add_artificial_logs(loggerMemApp, app, 6);
		String[] logStrings = app.getLogs();
		for (int i = 0; i < logStrings.length; i++) {
			System.out.println(logStrings[i]);
			Assert.assertEquals("getlogs did not return accurate log message", exptAddedLogs[i].getRenderedMessage() + "\n", logStrings[i]);
		}
	}
	
	@Test
	public void getLogCount_accurate() {
		Logger loggerMemApp = createMemAppenderLogger("loggerGetLogCount1", "appGetLogCount1");
		MemAppender app = (MemAppender) loggerMemApp.getAppender("appGetLogCount1");
		LoggingEvent[] exptAddedLogs = add_artificial_logs(loggerMemApp, app, 7);
		Assert.assertEquals("log count did not match expt log ammount", 7, app.getLogCount());
	}

}
