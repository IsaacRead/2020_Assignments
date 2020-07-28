package test.nz.ac.vuw.swen301.a2.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import nz.ac.vuw.swen301.a2.server.LogsServlet;

public class TestGetLogs {
	
	

	
	@Test
	public void testnoParameters() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing query parameter
		LogsServlet servlet = new LogsServlet();
		servlet.doGet(request, response);
		assertEquals(400, response.getStatus());
	}

	@Test
	public void testInvalidParameters1() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("not valid parameter", "100");
		MockHttpServletResponse response = new MockHttpServletResponse();
		// wrong query parameter
		LogsServlet servlet = new LogsServlet();
		servlet.doGet(request, response);
		assertEquals(400, response.getStatus());
	}

	@Test
	public void testInvalidParameters2() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("limit", "-1");
		request.setParameter("level", "DEBUG");
		MockHttpServletResponse response = new MockHttpServletResponse();
		// wrong query parameter - limit must be 0 or greater
		LogsServlet servlet = new LogsServlet();
		servlet.doGet(request, response);
		assertEquals(400, response.getStatus());
	}

	@Test
	public void testInvalidParameters3() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("limit", "0");
		request.setParameter("level", "NONVALID");
		MockHttpServletResponse response = new MockHttpServletResponse();
		// wrong query parameter - level must be of Logj4 Level enum
		LogsServlet servlet = new LogsServlet();
		servlet.doGet(request, response);
		assertEquals(400, response.getStatus());
	}

	
	@Test
	public void testInvalidParameters4() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("limit", "99999999999999");
		request.setParameter("level", "WARN");
		MockHttpServletResponse response = new MockHttpServletResponse();
		// wrong query parameter - limit must be in bounds of integer
		LogsServlet servlet = new LogsServlet();
		servlet.doGet(request, response);
		assertEquals(400, response.getStatus());
	}
	@Test
	public void testValidRequestResponseCode() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("limit", "3");
		request.setParameter("level", "DEBUG");
		MockHttpServletResponse response = new MockHttpServletResponse();
		// good query parameter
		LogsServlet servlet = new LogsServlet();
		servlet.doGet(request, response);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void testReturnedValues1() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("limit", "3");
		request.setParameter("level", "DEBUG");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		LogsServlet servlet = new LogsServlet();
		servlet.doGet(request, response);
		//check that an empty json array is the response
		JSONArray result = new JSONArray(response.getContentAsString());
		assertEquals(0, result.length());
	}
	
	@Test
	public void testReturnedValues2() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("limit", "3");
		request.setParameter("level", "ALL");
		MockHttpServletResponse response = new MockHttpServletResponse();

		LogsServlet servlet = new LogsServlet();
		UUID id = UUID.randomUUID();
		String message = "test Message";
		Instant timeStamp = Instant.now();
		String timeString = timeStamp.toString();
		String thread = "main";
		String logger = "testLogger";
		String level = "WARN";
		String errorDetails = "";
		//create log and add it to server logs list
		
		JSONObject log = testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
		
		servlet.addLog(log);
		
		servlet.doGet(request, response);
		JSONArray result = new JSONArray(response.getContentAsString());
		//System.out.print(result);
		//check only 1 log is returned and that its JSON is the same as the one genereated
		assertEquals(1, result.length());		
		//System.out.println(log.toString());
		//System.out.println(result.getJSONObject(0).toString());		
		assertTrue(testUtils.JSONEquals(log, result.getJSONObject(0)));		
	}
	
	//Check the amount of logs returned are the amount specified by limit 
	@Test
	public void testLimitParameter() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("limit", "42");
		request.setParameter("level", "ALL");
		MockHttpServletResponse response = new MockHttpServletResponse();

		LogsServlet servlet = new LogsServlet();
	
		for (int i = 0; i < 100; i++) {
			UUID id = UUID.randomUUID();
			String message = "these logs should not be returned";
			Instant timeStamp = Instant.now();
			String timeString = timeStamp.toString();
			String thread = "main";
			String logger = "testLogger";
			String level = "WARN";
			String errorDetails = "";
			//create log and add it to server logs list
			JSONObject log =  testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
			servlet.addLog(log);
		}
		LinkedList<JSONObject> addedLogs = new LinkedList<JSONObject>();
		for (int i = 0; i < 42; i++) {
			UUID id = UUID.randomUUID();
			String message = "these logs should be returned" + String.valueOf(i);
			Instant timeStamp = Instant.now();
			String timeString = timeStamp.toString();
			String thread = "main";
			String logger = "testLogger";
			String level = "WARN";
			String errorDetails = "";
			//create log and add it to server logs list
			JSONObject log =  testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
			//store these most recent to check the logs returned by doGet are correct
			addedLogs.add(log);
			servlet.addLog(log);
		}
		
		
		servlet.doGet(request, response);
		JSONArray result = new JSONArray(response.getContentAsString());
		//System.out.print(result);
		//check 42 logs are returned as specified by limit
		assertEquals(42, result.length());		
		//check logs returned equal the 42 most recently added, return is in reverse order with most recent at index 0
		for (int i = 0; i < 42; i++) {
			assertTrue(testUtils.JSONEquals(addedLogs.get(i), result.getJSONObject(addedLogs.size()- (1 + i))));
		}				
	}
	
	//Check the logs returned greater or same severity of specified level
	@Test
	public void testLevelParameter() throws IOException {
		LogsServlet.logs.clear();

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("limit", "1000");
		request.setParameter("level", "WARN");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		LogsServlet servlet = new LogsServlet();
	
		for (int i = 0; i < 10; i++) {
			UUID id = UUID.randomUUID();
			String message = "these logs should not be returned";
			Instant timeStamp = Instant.now();
			String timeString = timeStamp.toString();
			String thread = "main";
			String logger = "testLogger";
			String level = "DEBUG";
			String errorDetails = "";
			//create log and add it to server logs list
			JSONObject log =  testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
			servlet.addLog(log);
		}
		LinkedList<JSONObject> addedLogs = new LinkedList<JSONObject>();
		for (int i = 0; i < 10; i++) {
			UUID id = UUID.randomUUID();
			String message = "these logs should be returned" + String.valueOf(i);
			Instant timeStamp = Instant.now();
			String timeString = timeStamp.toString();
			String thread = "main";
			String logger = "testLogger";
			String level = "WARN";
			String errorDetails = "";
			//create log and add it to server logs list
			JSONObject log =  testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
			//store these most recent to check the logs returned by doGet are correct
			addedLogs.add(log);
			servlet.addLog(log);
		}
		for (int i = 0; i < 10; i++) {
			UUID id = UUID.randomUUID();
			String message = "these logs should not be returned";
			Instant timeStamp = Instant.now();
			String timeString = timeStamp.toString();
			String thread = "main";
			String logger = "testLogger";
			String level = "TRACE";
			String errorDetails = "";
			//create log and add it to server logs list
			JSONObject log =  testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
			servlet.addLog(log);
		}
		for (int i = 0; i < 10; i++) {
			UUID id = UUID.randomUUID();
			String message = "these logs should not be returned";
			Instant timeStamp = Instant.now();
			String timeString = timeStamp.toString();
			String thread = "main";
			String logger = "testLogger";
			String level = "TRACE";
			String errorDetails = "";
			//create log and add it to server logs list
			JSONObject log =  testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
			servlet.addLog(log);
		}
		for (int i = 0; i < 10; i++) {
			UUID id = UUID.randomUUID();
			String message = "these logs should be returned" + String.valueOf(i);
			Instant timeStamp = Instant.now();
			String timeString = timeStamp.toString();
			String thread = "main";
			String logger = "testLogger";
			String level = "ERROR";
			String errorDetails = "";
			//create log and add it to server logs list
			JSONObject log =  testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
			//store these most recent to check the logs returned by doGet are correct
			addedLogs.add(log);
			servlet.addLog(log);
		}
		for (int i = 0; i < 10; i++) {
			UUID id = UUID.randomUUID();
			String message = "these logs should be returned" + String.valueOf(i);
			Instant timeStamp = Instant.now();
			String timeString = timeStamp.toString();
			String thread = "main";
			String logger = "testLogger";
			String level = "FATAL";
			String errorDetails = "";
			//create log and add it to server logs list
			JSONObject log =  testUtils.createLogJSON(id.toString(), message, timeString, thread, logger, level, errorDetails);
			//store these most recent to check the logs returned by doGet are correct
			addedLogs.add(log);
			servlet.addLog(log);
		}
		
		
		servlet.doGet(request, response);
		JSONArray result = new JSONArray(response.getContentAsString());
		//System.out.print(result);
		//check 42 logs are returned as specified by limit
		assertEquals(addedLogs.size(), result.length());		
		//check logs returned equal the 42 most recently added, return is in reverse order with most recent at index 0
		for (int i = 0; i < addedLogs.size(); i++) {
			assertTrue(testUtils.JSONEquals(addedLogs.get(i), result.getJSONObject(addedLogs.size()- (1 + i))));
		}				
	}
}
