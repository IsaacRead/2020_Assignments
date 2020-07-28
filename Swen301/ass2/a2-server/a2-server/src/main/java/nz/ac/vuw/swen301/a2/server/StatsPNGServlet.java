package nz.ac.vuw.swen301.a2.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONObject;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class StatsPNGServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		List<List<String>> logCells = LogsStatistics.logsToNestedList();

		List<JSONObject> serverLogs = LogsServlet.getAllLogs();
		HashMap<String, Integer> levelCount = new HashMap<>();
		for (JSONObject log : serverLogs) {
			String currLevel = log.getString("level");
			if (levelCount.containsKey(currLevel)) {
				int currCount = levelCount.get(currLevel).intValue();
				currCount++;
				levelCount.put(currLevel, currCount);
			} else {
				levelCount.put(currLevel, 1);
			}
		}

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (String key : levelCount.keySet()) {
		    dataset.addValue(levelCount.get(key), "level", key);
		}
		
		JFreeChart barChart = ChartFactory.createBarChart("Log Stats", "Level", "Amount", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		
		OutputStream out = response.getOutputStream();

		ChartUtilities.writeChartAsPNG(out,barChart,500,500);
	}

}
