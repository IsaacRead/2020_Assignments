package nz.ac.vuw.swen301.a2.client;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.json.JSONObject;

public class Resthome4LogsAppender extends AppenderSkeleton {

	private String logServiceURL = "http://localhost:8080/resthome4logs/logs";
	CloseableHttpClient client = HttpClients.createDefault();
	HttpPost httpPost = new HttpPost(logServiceURL);

	public Resthome4LogsAppender() {

	}

	public void close() {
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean requiresLayout() {

		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		try {

			String logStr = CreateLogJSON(event);
			StringEntity requestEntity = new StringEntity(logStr, ContentType.APPLICATION_JSON);

			HttpPost postMethod = new HttpPost(logServiceURL);
			postMethod.setEntity(requestEntity);

			HttpResponse rawResponse = client.execute(postMethod);
			System.out.println(rawResponse.getStatusLine().getStatusCode());

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public String CreateLogJSON(LoggingEvent event) {
		JSONObject logJson = new JSONObject();
		logJson.put("id", UUID.randomUUID());
		logJson.put("message", event.getRenderedMessage());
		long timeLong = event.getTimeStamp();
		Instant inst = Instant.ofEpochMilli(timeLong);
		String formatedTime = inst.toString();
		logJson.put("timestamp", formatedTime);
		logJson.put("thread", event.getThreadName());
		logJson.put("logger", event.getLoggerName());
		logJson.put("level", event.getLevel());

		String[] errorDetails = event.getThrowableStrRep();
		String errorStr = "";
		if (errorDetails != null) {
			for (int i = 0; i < errorDetails.length; i++) {
				if (errorDetails[i] != null) {
					errorStr += errorDetails[i] + "	";
				}
			}
		}
		logJson.put("errorDetails", errorStr);
		return logJson.toString();
	}

	public String getLogServiceURL() {
		return logServiceURL;
	}

	public void setLogServiceURL(String logServiceURL) {
		this.logServiceURL = logServiceURL;
	}

}
