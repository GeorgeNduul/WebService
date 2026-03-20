///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */

package myRESTws;


//**
// *
// * @author t0337312
// */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExternalApiService {

    private static final String GEONAMES_USER = "t0337312"; 
    private static final String WEATHER_KEY = "d60973c5d7ded4bc5f445679e133aeb6"; 

    /**
     * FEATURE E: Get Weather (Public Information)
     */
    
    //URL Encoding: URLEncoder.encode(city, "UTF-8") ensures that spaces in city names (like "New York") don't break the web address.
    //It extracts the temp and description.
//It takes the timezone offset (seconds from UTC) and adds it to the current UTC time to calculate exactly what time it is at the event location.
    // converting raw Unix-style offsets into a human readable HH:mm format using the java.time api  
    
   public String getWeather(String city) {
    try {
        String encodedCity = java.net.URLEncoder.encode(city, "UTF-8");
        String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" 
                           + encodedCity + "&units=metric&appid=" + WEATHER_KEY;
        
        String resp = makeRequest(urlString);
        if (resp != null) {
            JSONObject json = new JSONObject(resp);
            
            double temp = json.getJSONObject("main").getDouble("temp");
            String desc = json.getJSONArray("weather").getJSONObject(0).getString("description");
            
            // 1. Get the Emoji based on the description
            String emoji = getWeatherEmoji(desc);

            // 2. Calculate Local Time
            long timezoneOffsetSeconds = json.getLong("timezone");
            java.time.OffsetDateTime localTime = java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC)
                                                 .plusSeconds(timezoneOffsetSeconds);
            String formattedTime = localTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

            // 3. Return the "Beautified" string
            return emoji + " " + temp + "°C, " + desc + " (Local Time: " + formattedTime + ")";
        }
    } catch (Exception e) { 
        return "Weather & Time info unavailable"; 
    }
    return "Weather & Time info unavailable";
}

// Helper method to map descriptions to emojis
   // simple form of natural language processing
   //it uses .contain()to find keywords If the API returns "heavy intensity rain," the code sees the word "rain" and picks the 🌧️ emoji.
private String getWeatherEmoji(String desc) {
    desc = desc.toLowerCase();
    if (desc.contains("clear") || desc.contains("sun")) return "☀️";
    if (desc.contains("cloud")) return "☁️";
    if (desc.contains("rain") || desc.contains("drizzle")) return "🌧️";
    if (desc.contains("thunder")) return "⛈️";
    if (desc.contains("snow")) return "❄️";
    if (desc.contains("mist") || desc.contains("fog") || desc.contains("haze")) return "🌫️";
    return "🌍"; // Default emoji
}
    /**
     * FEATURE E: Get GeoNames (Places of Interest)
     */

// this adds contextual value instead of just seeing a map the user can see what else is interesting 
// it sends lat and long which returns a findNearbyWikipediaJSON of list entries in that location
    public List<String> getLandmarks(double lat, double lng) {
        List<String> landmarks = new ArrayList<>();
        try {
            String urlString = "http://api.geonames.org/findNearbyWikipediaJSON?lat=" 
                                + lat + "&lng=" + lng + "&username=" + GEONAMES_USER;
            String resp = makeRequest(urlString);
            if (resp != null) {
                JSONObject json = new JSONObject(resp);
                JSONArray array = json.getJSONArray("geonames");
                // only returns 3 landmarks 
                for (int i = 0; i < Math.min(array.length(), 3); i++) {
                    landmarks.add(array.getJSONObject(i).getString("title"));
                }
            }
        } catch (Exception e) { landmarks.add("Landmarks unavailable"); }
        return landmarks;
    }
// this is a generic helper method that handles the actual internet connection
 // im using a string builder because its more memory effiecient than using a  string to join many texts lof line together in a loop
    private String makeRequest(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            BufferedReader rb = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rb.readLine()) != null) sb.append(line);
            rb.close();
            return sb.toString();
        }
        return null;
    }
}