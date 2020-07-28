package nz.ac.vuw.swen301.a2.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
//import java.lang.System.Logger.Level;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONArray;
import org.json.JSONObject;

public class LogsServlet extends HttpServlet {
	public static List<JSONObject> logs = new LinkedList<JSONObject>();

	public static List<JSONObject> getAllLogs() {
		
		List<JSONObject> logClone = new LinkedList<JSONObject>();
		for (int i = 0; i < logs.size(); i++) {
			logClone.add(new JSONObject(logs.get(i).toString()));
		}
		return logClone;
	}
	
	public LogsServlet() {}
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			BufferedReader reqReader = req.getReader();
			String reqStr = reqReader.readLine();
			
			reqReader.close();
			System.out.println(reqStr);
			if (reqStr == null) {
				resp.setStatus(400);
				
				return;
			}
			JSONObject logJson = new JSONObject(reqStr);
			resp.setStatus(addLog(logJson));
		} catch (Exception e) {
			e.printStackTrace();
			
			resp.setStatus(400);
			return;
		}

	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		String limitReq = request.getParameter("limit");
		String levelReq = request.getParameter("level");
		System.out.println(limitReq);
		Level minLevel;
		
		int limit;
		try {
			minLevel = Level.toLevel(levelReq, null);
			if (minLevel == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			limit = Integer.parseInt(limitReq);
			if (limit < 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		JSONArray logsjson = new JSONArray();
		int logsFound = 0;
		int count = 0;
		while(logsFound < limit && count < logs.size()) {
			JSONObject currLog = logs.get(count);
			Level currLevel = Level.toLevel(currLog.getString("level"));
			if (currLevel.isGreaterOrEqual(minLevel)) {
				logsjson.put(currLog);
				logsFound++;
			}
			count++;
		}
		out.print(logsjson);
	
		out.close();
	}

	public static int addLog(JSONObject log) {

		if (log.length() != 7) {

			return 400;
		}
		try {

			String idStr = log.getString("id");
			String messageStr = log.getString("message");
			String timeStr = log.getString("timestamp");
			String threadStr = log.getString("thread");
			String loggerStr = log.getString("logger");
			String levelStr = log.getString("level");
			String errorStr = log.getString("errorDetails");

			Level level = Level.toLevel(levelStr, null);
			if (level == null) {
				return 400;

			}
			Instant inputTime = Instant.parse(timeStr);
			UUID inputUid = UUID.fromString(idStr);
			int index = 0;
			boolean indexFound = false;
			for (int i = 0; i < logs.size(); i++) {
				Instant compTime = Instant.parse(logs.get(i).getString("timestamp"));
				UUID currUid = UUID.fromString(logs.get(i).getString("id"));
				if (currUid.equals(inputUid)) {
					return 409;
				}
				if (inputTime.isAfter(compTime) && !indexFound) {
					index = i;
					indexFound = true;
				}
			}
			if (indexFound == false) {
				logs.add(log);
			} else {
				logs.add(index, log);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return 400;
		}
		return 201;
	}

}
