package test.ac.vuw.swen301.a1;

import org.json.*;
import org.junit.Test;
import org.junit.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;


import org.apache.log4j.*;
//import nz.ac.vuw.swen301.a1.JSONLayout;

public class JSONLayoutTest {

	public static Logger createJSONLogger(String name, String file) {
		Logger logger = Logger.getLogger(name);
		try {
			logger.addAppender(new org.apache.log4j.FileAppender(new nz.ac.vuw.swen301.a1.JSONLayout(), file, false));
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("creating logger with json layout failed");
		}
		return logger;
	}

	

	public static void testJSONLogger(String level) {
		for (int i = 0; i < 5; i++) {
			String loggerName = String.format("%s_Logger%d",level, i);
			String fileName = String.format("%sLog%d.json",level, i);
			Logger logger = createJSONLogger(loggerName, fileName);
			String message = String.format("%s_test_%d log message", level, i);
			switch (level) {
			case "TRACE":
				logger.trace(message);
				break;
			case "DEBUG":
				logger.debug(message);
				break;
			case "INFO":
				logger.info(message);
				break;
			case "WARN":
				logger.warn(message);
				break;
			case "ERROR":
				logger.error(message);
				break;	
			case "FATAL":
				logger.fatal(message);
				break;	
			default:
				Assert.fail("Level was not correct");
				break;
			}
			long logTime = Instant.now().toEpochMilli();
			File file = new File(fileName);
			String jsonData = testUtils.readFile(file);
			file.delete();
			JSONObject jobj = new JSONObject(jsonData);
			testUtils.checkJSONMatch(jobj, level, loggerName, logTime, Thread.currentThread().getName(), message);
		}
	}
	
	@Test
	public void trace_test() {
		testJSONLogger("TRACE");
	}
	@Test
	public void debug_test() {
		testJSONLogger("DEBUG");
	}
	@Test
	public void info_test() {
		testJSONLogger("INFO");
	}
	@Test
	public void warn_test() {
		testJSONLogger("WARN");
	}
	@Test
	public void error_test() {
		testJSONLogger("ERROR");
	}
	@Test
	public void fatal_test() {
		testJSONLogger("FATAL");
	}
	

}
