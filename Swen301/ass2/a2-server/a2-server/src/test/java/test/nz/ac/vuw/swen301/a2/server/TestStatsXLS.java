package test.nz.ac.vuw.swen301.a2.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import nz.ac.vuw.swen301.a2.server.LogsServlet;
import nz.ac.vuw.swen301.a2.server.LogsStatistics;
import nz.ac.vuw.swen301.a2.server.StatsXLSServlet;

public class TestStatsXLS {

	public void checkXLS(MockHttpServletResponse response) throws IOException {
		Workbook workbook = new HSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()));
		List<List<String>> expectedCells = LogsStatistics.logsToNestedList();
		

		Sheet datatypeSheet = workbook.getSheetAt(0);
		Iterator<Row> iterator = datatypeSheet.iterator();
		
		int currRow = 0;
		while (iterator.hasNext()) {
			int currCol = 0;
			Row currentRow = iterator.next();
			Iterator<Cell> cellIterator = currentRow.iterator();
			while (cellIterator.hasNext()) {
				Cell currentCell = cellIterator.next();

				String cellVal = currentCell.getStringCellValue();
				// System.out.print(cellVal + "--");
				assertEquals(
						"column:" + String.valueOf(currCol) + " row:" + String.valueOf(currRow) + " was not correct",
						expectedCells.get(currRow).get(currCol), cellVal);
				currCol++;
			}
			// System.out.println();

			currRow++;
		}
		assertEquals("did not have the right number of rows", expectedCells.size(), currRow);

	}

	@Test
	public void testEmptylogstoXLS() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsXLSServlet servlet = new StatsXLSServlet();
		servlet.doGet(request, response);
		checkXLS(response);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void test1logtoXLS() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsXLSServlet servlet = new StatsXLSServlet();
		JSONObject mostRecentLog = testUtils.createLogJSON(UUID.randomUUID().toString(), "most recent",
				Instant.parse("2020-06-10T07:06:56.275654Z").toString(), "main", "testLogger", "WARN", "");

		servlet.doGet(request, response);
		checkXLS(response);
		assertEquals(200, response.getStatus());
	}

	@Test
	public void testMultiplelogtoXLS1() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsXLSServlet servlet = new StatsXLSServlet();
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
		checkXLS(response);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testMultiplelogtoXLS2() throws IOException, ServletException {
		LogsServlet.logs.clear();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		// missing input
		StatsXLSServlet servlet = new StatsXLSServlet();
		
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
		checkXLS(response);
		assertEquals(200, response.getStatus());
	}

}
