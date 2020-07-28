package test.ac.vuw.swen301.a1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.json.JSONObject;
import org.junit.Assert;

public class testUtils {

	public static String readFile(File file) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			result = sb.toString();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		return result;
	}
	
	public static void checkJSONMatch(JSONObject jobj, String exptLevel, String exptLoggerName, Long exptLogTime, String exptThread,
			String exptMessage) {
		
		Assert.assertEquals("JSON file does not match log message generated", exptLevel, jobj.getString("level") );
		Assert.assertEquals("JSON file does not match log message generated", exptLoggerName, jobj.getString("logger") );
		Assert.assertEquals("JSON file does not match log message generated", exptLogTime, jobj.getLong("starttime") , 40);
		Assert.assertEquals("JSON file does not match log message generated", exptThread, jobj.getString("thread") );
		Assert.assertEquals("JSON file does not match log message generated", exptMessage, jobj.getString("message") );

	}
}
