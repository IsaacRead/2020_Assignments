package test.nz.ac.vuw.swen301.a2.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import nz.ac.vuw.swen301.a2.server.LogsServlet;
import nz.ac.vuw.swen301.a2.server.LogsStatistics;
import nz.ac.vuw.swen301.a2.server.StatsCSVServlet;
import nz.ac.vuw.swen301.a2.server.StatsXLSServlet;

public class TestStatsCSV {
	public void checkCSV(MockHttpServletResponse response) throws IOException {
		List<List<String>> expectedLogs = LogsStatistics.logsToNestedList();
		String csv = response.getContentAsString();
		//System.out.print(csv);
		String[] rows = csv.split("\n");
		assertEquals("did not have the right number of rows", expectedLogs.size(), rows.length);
		int rowCount = 0;
		
		for (String row : rows) {
			int colCount = 0;
			String[] cells = row.split("\t");
			for (String cell : cells) {
				
				assertEquals("row:" + String.valueOf(rowCount) + "col:" + String.valueOf(colCount) + " was not as expected" ,expectedLogs.get(rowCount).get(colCount), cell);
				colCount++;
			}
			rowCount++;			
		}		
	}
	

	@Test
	public void testEmptylogstoCSV() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsCSVServlet servlet = new StatsCSVServlet();
		servlet.doGet(request, response);
		checkCSV(response);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void test1logtoCSV() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsCSVServlet servlet = new StatsCSVServlet();
		JSONObject mostRecentLog = testUtils.createLogJSON(UUID.randomUUID().toString(), "most recent",
				Instant.parse("2020-06-10T07:06:56.275654Z").toString(), "main", "testLogger", "WARN", "");

		servlet.doGet(request, response);
		checkCSV(response);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void testMultiplelogtoCSV1() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsCSVServlet servlet = new StatsCSVServlet();
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "message1",
				Instant.parse("2020-06-10T07:06:56.275654Z").toString(), "main", "testLogger2", "WARN", ""));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "message3",
				Instant.parse("2020-05-10T07:06:56.275654Z").toString(), "main", "testLogger", "ERROR", ""));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "mes4",
				Instant.parse("2020-04-10T07:06:55.275654Z").toString(), "main", "testLogger", "ERROR", ""));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "least recent",
				Instant.parse("2020-04-10T07:06:55.275654Z").toString(), "main", "logg3", "ERROR", ""));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "least recent",
				Instant.parse("2020-04-10T07:06:55.275654Z").toString(), "main", "logger2", "WARN", ""));
		servlet.doGet(request, response);
		checkCSV(response);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testMultiplelogtoCSV2() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsCSVServlet servlet = new StatsCSVServlet();
		
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "most recent",
				Instant.parse("2010-06-10T07:06:53.271254Z").toString(), "main", "testLogger", "WARN", ""));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "less recent",
				Instant.parse("2020-05-10T07:06:36.275654Z").toString(), "main", "testLogger", "FATAL", ""));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "least recent",
				Instant.parse("2020-04-11T07:04:55.275654Z").toString(), "main", "testLogger", "DEBUG", "mes"));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "least recent",
				Instant.parse("2020-04-12T07:06:55.275654Z").toString(), "otherThread", "testLogger", "TRACE", ""));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "least recent",
				Instant.parse("2020-04-13T07:06:55.275654Z").toString(), "otherThread", "testLogger1", "ERROR", ""));
		LogsServlet.addLog(testUtils.createLogJSON(UUID.randomUUID().toString(), "least recent",
				Instant.parse("2020-04-13T07:06:55.275654Z").toString(), "otherThread", "testLogger1", "INFO", ""));
		servlet.doGet(request, response);
		checkCSV(response);
		assertEquals(200, response.getStatus());
	}
}
