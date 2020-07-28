package nz.ac.vuw.swen301.a2.server;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

public class LogsStatistics {
	
	private static boolean updateStat(String statInput, int inputCol, int startIndex, int endIndex,
			List<List<String>> logCells) {
		// String currLevel = log.getString("level");
		int inputRow = 0;
		for (int i = startIndex; i < endIndex; i++) {
			if (logCells.get(i).get(0).equals(statInput)) {
				inputRow = i;
			}
		}
		boolean insertedRow = false;
		if (inputRow == 0) {
			inputRow = startIndex;
			logCells.add(inputRow, new ArrayList<String>() {
				{
					add(statInput);
				}
			});
			for (int i = 1; i < logCells.get(0).size(); i++) {
				logCells.get(inputRow).add("0");
			}
			insertedRow = true;
		}
//		System.out.println(statInput);
//		System.out.println(inputRow);
//		System.out.println(inputCol);
		int count = Integer.parseInt(logCells.get(inputRow).get(inputCol));
		count++;
//		System.out.println(count);
		logCells.get(inputRow).set(inputCol, String.valueOf(count));
		return insertedRow;
	}

	private static void printlogCells(List<List<String>> logCells) {
		System.out.print("\n\n");
		for (int i = 0; i < logCells.size(); i++) {
			for (int j = 0; j < logCells.get(i).size(); j++) {
				System.out.print(logCells.get(i).get(j));
				System.out.print(" ");
			}
			System.out.print("\n");
		}
	}

	public static List<List<String>> logsToNestedList(){	
		//System.out.println(LogsServlet.logs.size());
		List<List<String>> logCells = new ArrayList<List<String>>();

		logCells.add(new ArrayList<String>() {
			{
				add("Date");
			}
		});
		logCells.add(new ArrayList<String>() {
			{
				add("TRACE");
			}
		});
		logCells.add(new ArrayList<String>() {
			{
				add("DEBUG");
			}
		});
		logCells.add(new ArrayList<String>() {
			{
				add("INFO");
			}
		});
		logCells.add(new ArrayList<String>() {
			{
				add("WARN");
			}
		});
		logCells.add(new ArrayList<String>() {
			{
				add("ERROR");
			}
		});
		logCells.add(new ArrayList<String>() {
			{
				add("FATAL");
			}
		});

		logCells.add(new ArrayList<String>() {
			{
				add("");
			}
		});

		logCells.add(new ArrayList<String>() {
			{
				add("");
			}
		});
		logCells.add(new ArrayList<String>() {
			{
				add("");
			}
		});

		int loggerRowStart = 8;
		int threadRowStart = 9;

		List<JSONObject> serverLogs = LogsServlet.getAllLogs();
		for (JSONObject log : serverLogs) {
			try {
		
			Instant logTime = Instant.parse(log.getString("timestamp"));
			Date myDate = Date.from(logTime);
			// DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			// String formattedDate = myFormatObj.format(myDate);
			String formattedDate = formatter.format(myDate);
			// Date insertDate = (Date) myFormatObj.parse(formattedDate);
			Date insertDate;
		
			insertDate = (Date) formatter.parse(formattedDate);
				int collIndex = 0;

				for (int i = 1; i < logCells.get(0).size(); i++) {
					// Date cellDate = (Date) myFormatObj.parse(logCells.get(i).get(0));

					Date cellDate = (Date) formatter.parse(logCells.get(0).get(i));
					if (insertDate.before(cellDate)) {
						logCells.get(0).add(i, formattedDate);
						for (int j = 1; j < logCells.size(); j++) {
							if (!logCells.get(j).get(0).equals("")) {
								logCells.get(j).add(i, "0");
							}
						}
						collIndex = i;
						break;
					} else if (insertDate.equals(cellDate)) {
						collIndex = i;
						break;
					}
				}
				if (collIndex == 0) {
					collIndex = logCells.get(0).size();
					logCells.get(0).add(formattedDate);
					for (int j = 1; j < logCells.size(); j++) {
						if (!logCells.get(j).get(0).equals("")) {
							logCells.get(j).add("0");
						}
					}
				}
				
				if (updateStat(log.getString("level"), collIndex, 1, loggerRowStart, logCells)) {
					loggerRowStart++;
					threadRowStart++;
				}
				
				if (updateStat(log.getString("logger"), collIndex, loggerRowStart, threadRowStart, logCells)) {
					threadRowStart++;
				}
				
				updateStat(log.getString("thread"), collIndex, threadRowStart, logCells.size(), logCells);
			//printlogCells(logCells);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}			
		}
		return logCells;
		
	}

}
