package nz.ac.vuw.swen301.a1;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONObject;

public class JSONLayout extends Layout {

	public void activateOptions() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public String format(LoggingEvent event) {
		JSONObject jo = new JSONObject();
		jo.put("logger", event.getLoggerName());
		jo.put("level", event.getLevel());
		jo.put("starttime", event.getTimeStamp()); 
		jo.put("thread", event.getThreadName());
		jo.put("message", event.getRenderedMessage());
		return jo.toString(3) + "\n";
	}
	

	@Override
	public boolean ignoresThrowable() {
		// TODO Auto-generated method stub
		return true;
	}

}

