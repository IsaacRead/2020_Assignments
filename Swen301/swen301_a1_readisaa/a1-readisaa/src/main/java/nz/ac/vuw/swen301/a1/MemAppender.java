package nz.ac.vuw.swen301.a1;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONArray;
import org.json.JSONObject;

public class MemAppender extends AppenderSkeleton implements MemAppenderMBean {
	//util.list??
	//name property already exists???
	private List<LoggingEvent> logs = new LinkedList<LoggingEvent>(); 
	private long maxSize = 1000;
	private long discardedLogCount;
	
	public long getDiscardedLogCount() {
		return discardedLogCount;
	}
	
	public long getMaxSize() {
		return maxSize;
	}
	
	public boolean setMaxSize(long maxSize) {
		if (maxSize < 0) {
			return false;
		}else {
		this.maxSize = maxSize;
		checkMaxSize();
		return true;
		}
	}
	
	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		logs.add(event);
		checkMaxSize();
	}
	
	private void checkMaxSize() {	
		while (getLogCount() > maxSize){
			logs.remove(0);
			discardedLogCount++;
		}
	}
	
	public List<LoggingEvent> getCurrentLogs(){
		return Collections.unmodifiableList(logs);
	}
	
	public void exportToJSON(String fileName) throws FileNotFoundException {
		JSONLayout jLayout = new JSONLayout();
		JSONArray jArr = new JSONArray();
		for (int i = 0; i < getLogCount(); i++) {
			JSONObject jObj = new JSONObject(jLayout.format(logs.get(i)));
			jArr.put(jObj);
		}
		PrintWriter out = new PrintWriter(fileName);
		out.print(jArr.toString(3));
		out.close();
		
		
	}

	@Override
	public String[] getLogs() {
		String[] logStrings = new String[(int)getLogCount()];
		for (int i = 0; i < getLogCount(); i++) {
			PatternLayout patLayout = new PatternLayout();
			logStrings[i] = patLayout.format(logs.get(i));
		}
		return logStrings;
	}

	@Override
	public long getLogCount() {
		return logs.size();
	}

}
