package test.nz.ac.vuw.swen301.a2.server;

import java.util.Iterator;

import org.json.JSONObject;

public class testUtils {
	public static JSONObject createLogJSON(String id, String message, String timeStamp, String thread, String logger, String level, String errorDetails) {
		JSONObject log = new JSONObject();
		log.put("id", id);		
		log.put("message", message);
		log.put("timestamp", timeStamp);
		log.put("thread", thread);
		log.put("logger", logger);
		log.put("level", level);
		log.put("errorDetails", errorDetails);
		return log;
	}
	
	public static boolean JSONEquals(JSONObject json1, JSONObject json2) {
		if(json1.length() != json2.length()) {
			System.out.println("1");
			return false;
		}
		Iterator<String> keys = json1.keys();
		while (keys.hasNext()) {
            String key = keys.next();
            if (!(json1.getString(key).equals(json2.getString(key)))) {
            	System.out.println(key);
            	System.out.println(json1.getString(key) );
            	System.out.println(json2.getString(key) );
            	return false;
            }
		}
		return true;
	}
}
