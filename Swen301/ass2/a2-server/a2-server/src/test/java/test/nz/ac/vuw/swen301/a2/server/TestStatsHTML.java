package test.nz.ac.vuw.swen301.a2.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import nz.ac.vuw.swen301.a2.server.LogsServlet;
import nz.ac.vuw.swen301.a2.server.LogsStatistics;
import nz.ac.vuw.swen301.a2.server.StatsCSVServlet;
import nz.ac.vuw.swen301.a2.server.StatsServlet;

public class TestStatsHTML {

	public void checkHTML(MockHttpServletResponse response) throws IOException {
		Document doc = Jsoup.parse(response.getContentAsString());
		//Element link = doc.select("a").first();
		Element table = doc.select("table").get(0);
		Elements rows = table.select("tr");
		List<List<String>> logList = LogsStatistics.logsToNestedList();
		assertEquals("did not have the right number of rows", logList.size(), rows.size());
		
		
		Elements titles  = rows.get(0).select("th");
		for (int i = 0; i < titles.size(); i++) {
			assertEquals("row:" + String.valueOf(i) + "col:" + String.valueOf(i) + " was not as expected",logList.get(0).get(i), titles.get(i).ownText());
		}
		
		for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
		    Element row = rows.get(i);
		    Elements cols = row.select("td");
		    for (int j = 0; j < cols.size(); j++) {		    	
		    	 assertEquals("row:" + String.valueOf(i) + "col:" + String.valueOf(j) + " was not as expected",logList.get(i).get(j), cols.get(j).ownText());
			} 
		
		}

		
	}
	@Test
	public void testEmptylogs() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsServlet servlet = new StatsServlet();
		servlet.doGet(request, response);
		checkHTML(response);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void test1log() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsServlet servlet = new StatsServlet();
		JSONObject mostRecentLog = testUtils.createLogJSON(UUID.randomUUID().toString(), "most recent",
				Instant.parse("2020-06-10T07:06:56.275654Z").toString(), "main", "testLogger", "WARN", "");

		servlet.doGet(request, response);
		checkHTML(response);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void testMultiplelog() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsServlet servlet = new StatsServlet();
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
		checkHTML(response);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testMultiplelog2() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsServlet servlet = new StatsServlet();
		
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
		checkHTML(response);
		assertEquals(200, response.getStatus());
	}
}
