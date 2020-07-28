package nz.ac.vuw.engr301.group7.mission_control_proj;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
final class Wind
{
	public double speed,direction;
	
	public Wind(double speed, double direction) {
		this.speed = speed;
		this.direction = direction;
	}
}
public class HTTP_Request {
	public static void main(String[] args) {
     try {
    	 String APIKey = "8d77e42c64b50633eb0a2dde20f2d765";
    	 
    	 Scanner sc = new Scanner(System.in);
    	 System.out.println("Enter Latitude: ");
    	 Double lat = sc.nextDouble();
    	 System.out.println("Enter Longitude: ");
    	 Double lon = sc.nextDouble();
         HTTP_Request.call_me(lat,lon,APIKey);
         sc.close();
        } catch (Exception e) {
         e.printStackTrace();
       }
     }
	   
public static Wind call_me(Double lat, Double lon, String APIKey) throws Exception {
	 
	 String url = "https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&appid="+APIKey;
     URL obj = new URL(url);
     HttpURLConnection con = (HttpURLConnection) obj.openConnection();
     // optional default is GET
     con.setRequestMethod("GET");
     //add request header
     con.setRequestProperty("User-Agent", "Mozilla/5.0");
     //int responseCode = con.getResponseCode();
     //System.out.println("\nSending 'GET' request to URL : " + url);
     //System.out.println("Response Code : " + responseCode);
     BufferedReader in = new BufferedReader(
             new InputStreamReader(con.getInputStream()));
     String inputLine;
     StringBuffer response = new StringBuffer();
     while ((inputLine = in.readLine()) != null) {
     	response.append(inputLine);
     }
     in.close();
     //Read JSON response and print
     JSONObject myResponse = new JSONObject(response.toString());
     /*
     System.out.println("--------------------------Input Request------------------------------------------");
     System.out.println("Latitude- "+myResponse.getDouble("lat"));
     System.out.println("Longitude- "+myResponse.getDouble("lon"));
     System.out.println("timezone- "+myResponse.getString("timezone"));
     
     System.out.println("--------------------------Output Response----------------------------------------");
     System.out.println("Wind Speed - "+ myResponse.getJSONObject("current").getDouble("wind_speed"));
     System.out.println("Wind Direction - "+ myResponse.getJSONObject("current").getDouble("wind_deg"));
     */
     
     double speed = myResponse.getJSONObject("current").getDouble("wind_speed");
     double direction = myResponse.getJSONObject("current").getDouble("wind_deg");
     return new Wind(speed,direction);
     
   }
}