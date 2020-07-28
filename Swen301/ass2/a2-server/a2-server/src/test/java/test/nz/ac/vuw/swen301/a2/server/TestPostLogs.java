package test.nz.ac.vuw.swen301.a2.server;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.pattern.LogEvent;
import org.json.JSONObject;
import org.junit.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import nz.ac.vuw.swen301.a2.server.LogsServlet;
public class TestPostLogs {

	@Test
	public void testNoInput() throws IOException, ServletException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		LogsServlet servlet = new LogsServlet();	
		servlet.doPost(request, response);
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testIncorrectInput1() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		//Logger logger = Logger.getLogger("logger");
		//LogEvent log = new LogEvent("category class", logger, Level.ERROR, "mesage1", null);
		LogsServlet servlet = new LogsServlet();	
		UUID id = UUID.randomUUID();
		String message = "test Message";
		Instant timeStamp = Instant.now();
		String timeString = timeStamp.toString();
		String thread = "main";
		String logger = "testLogger";
		String level = "INCCORECT";
		String errorDetails = "";
		//create log JSON		
		JSONObject log = testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
		//change to byte to send it in doPost content
		byte[] sendData = log.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		//check return code is 400 meaning invalid input
		assertEquals(400, response.getStatus());
		//check server log list contains no logs
		assertEquals(servlet.logs.size(), 0);

	}
	
	@Test
	public void testIncorrectInput2() throws IOException, ServletException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		//Logger logger = Logger.getLogger("logger");
		//LogEvent log = new LogEvent("category class", logger, Level.ERROR, "mesage1", null);
		LogsServlet servlet = new LogsServlet();	
		UUID id = UUID.randomUUID();
		String message = "test Message";
		Instant timeStamp = Instant.now();
		String timeString = timeStamp.toString();
		String thread = "main";
		String logger = "testLogger";
		String level = "WARN";
		String errorDetails = "";
		//create log JSON		
		JSONObject log = testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
		//make log json incorrect by removing id
		log.remove("id");
		//change to byte to send it in doPost content
		byte[] sendData = log.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		//check return code is 400 meaning invalid input
		assertEquals(400, response.getStatus());
		//check server log list contains no logs
		assertEquals(servlet.logs.size(), 0);

	}
	@Test
	public void testIncorrectInput3() throws IOException, ServletException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		//Logger logger = Logger.getLogger("logger");
		//LogEvent log = new LogEvent("category class", logger, Level.ERROR, "mesage1", null);
		LogsServlet servlet = new LogsServlet();	
		UUID id = UUID.randomUUID();
		//message shouldn't be allowed to be null, expect 400 response
		String message = null;
		Instant timeStamp = Instant.now();
		String timeString = timeStamp.toString();
		String thread = "main";
		String logger = "testLogger";
		String level = "WARN";
		String errorDetails = "";
		//create log JSON		
		JSONObject log = testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
		log.put("invalidKey", "value");
		//change to byte to send it in doPost content
		byte[] sendData = log.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		//check return code is 400 meaning invalid input
		assertEquals(400, response.getStatus());
		//check server log list contains no logs
		assertEquals(servlet.logs.size(), 0);

	}
	@Test
	public void testIncorrectInput4() throws IOException, ServletException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		//Logger logger = Logger.getLogger("logger");
		//LogEvent log = new LogEvent("category class", logger, Level.ERROR, "mesage1", null);
		LogsServlet servlet = new LogsServlet();	
		UUID id = UUID.randomUUID();
		//message shouldn't be allowed to be null, expect 400 response
		String message = "message";
		//set time string to something invalid, expect 400 response
		String timeString = "invalid";
		String thread = "main";
		String logger = "testLogger";
		String level = "WARN";
		String errorDetails = "";
		//create log JSON		
		JSONObject log = testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);

		//change to byte to send it in doPost content
		byte[] sendData = log.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		//check return code is 400 meaning invalid input
		assertEquals(400, response.getStatus());
		//check server log list contains no logs
		assertEquals(servlet.logs.size(), 0);

	}
	@Test
	public void testIncorrectInput5() throws IOException, ServletException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		LogsServlet servlet = new LogsServlet();	
		//send non json doPost request
		byte[] sendData = "invalid json".getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		//check return code is 400 meaning invalid input
		assertEquals(400, response.getStatus());
		//check server log list contains no logs
		assertEquals(servlet.logs.size(), 0);

	}
	
	@Test
	public void testDuplicateId() throws IOException, ServletException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		//Logger logger = Logger.getLogger("logger");
		//LogEvent log = new LogEvent("category class", logger, Level.ERROR, "mesage1", null);
		LogsServlet servlet = new LogsServlet();	
		UUID id = UUID.randomUUID();
		String message = "test Message";
		Instant timeStamp = Instant.now();
		String timeString = timeStamp.toString();
		String thread = "main";
		String logger = "testLogger";
		String level = "WARN";
		String errorDetails = "";
		//create log JSON		
		JSONObject log = testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
		//change to byte to send it in doPost content
		byte[] sendData = log.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		//send duplicate log
		request.setContent(sendData);
		servlet.doPost(request, response);
		//check return code is  409 meaning duplicate id
		assertEquals(409, response.getStatus());
		//check server log list contains our 1 log added
		assertEquals(servlet.logs.size(), 1);

	}
	
	@Test
	public void testInputCorrectLog() throws IOException, ServletException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		//Logger logger = Logger.getLogger("logger");
		//LogEvent log = new LogEvent("category class", logger, Level.ERROR, "mesage1", null);
		LogsServlet servlet = new LogsServlet();	
		UUID id = UUID.randomUUID();
		String message = "test Message";
		Instant timeStamp = Instant.now();
		String timeString = timeStamp.toString();
		String thread = "main";
		String logger = "testLogger";
		String level = "WARN";
		String errorDetails = "";
		//create log JSON		
		JSONObject log = testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
		//change to byte to send it in doPost content
		byte[] sendData = log.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		//check return code is 201 meaning items added
		assertEquals(201, response.getStatus());
		//check server log list contains our 1 log added
		assertEquals(servlet.logs.size(), 1);
		assertTrue(testUtils.JSONEquals(log, servlet.logs.get(0)));
	}
	
	@Test
	public void testInputIsOrderedByTimestamp() throws IOException, ServletException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		//Logger logger = Logger.getLogger("logger");
		//LogEvent log = new LogEvent("category class", logger, Level.ERROR, "mesage1", null);
		LogsServlet servlet = new LogsServlet();	
		//create log JSON		
		JSONObject mostRecentLog = testUtils.createLogJSON(UUID.randomUUID().toString(), "most recent", Instant.parse("2020-06-10T07:06:56.275654Z").toString(), "main", "testLogger", "WARN", "");
		JSONObject lessRecentLog = testUtils.createLogJSON(UUID.randomUUID().toString(), "less recent", Instant.parse("2019-06-10T07:06:56.275654Z").toString(), "main", "testLogger", "ERROR", "");
		JSONObject leastRecentLog = testUtils.createLogJSON(UUID.randomUUID().toString(), "least recent", Instant.parse("2019-06-10T07:06:55.275654Z").toString(), "main", "testLogger", "ERROR", "");

		//send logs out of order to check they are ordered by timestamp by server with most recent at index 0
		byte[] sendData = mostRecentLog.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		assertEquals(201, response.getStatus());
		sendData = leastRecentLog.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		assertEquals(201, response.getStatus());
		sendData = lessRecentLog.toString().getBytes("utf-8");
		request.setContent(sendData);
		servlet.doPost(request, response);
		assertEquals(201, response.getStatus());
		//check server log list contains our 3 logs added
		assertEquals(servlet.logs.size(), 3);
		//check logs are ordered by timestamp with most recent at index 0
		assertTrue(testUtils.JSONEquals(mostRecentLog, servlet.logs.get(0)));
		assertTrue(testUtils.JSONEquals(lessRecentLog, servlet.logs.get(1)));
		assertTrue(testUtils.JSONEquals(leastRecentLog, servlet.logs.get(2)));
	}
	
}
