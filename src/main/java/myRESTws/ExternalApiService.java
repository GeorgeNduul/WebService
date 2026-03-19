package myRESTws;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExternalApiService {

    private static final String GEONAMES_USER = "t0337312"; // Use your verified username
    private static final String WEATHER_KEY = "d60973c5d7ded4bc5f445679e133aeb6"; 

    /**
     * FEATURE E: Get Weather (Public Information)
     */
    public String getWeather(String city) {
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" 
                                + city + "&units=metric&appid=" + WEATHER_KEY;
            String resp = makeRequest(urlString);
            if (resp != null) {
                JSONObject json = new JSONObject(resp);
                double temp = json.getJSONObject("main").getDouble("temp");
                String desc = json.getJSONArray("weather").getJSONObject(0).getString("description");
                return temp + "°C, " + desc;
            }
        } catch (Exception e) { return "Weather info unavailable"; }
        return "Weather info unavailable";
    }

    /**
     * FEATURE E: Get GeoNames (Places of Interest)
     */
    public List<String> getLandmarks(double lat, double lng) {
        List<String> landmarks = new ArrayList<>();
        try {
            String urlString = "http://api.geonames.org/findNearbyWikipediaJSON?lat=" 
                                + lat + "&lng=" + lng + "&username=" + GEONAMES_USER;
            String resp = makeRequest(urlString);
            if (resp != null) {
                JSONObject json = new JSONObject(resp);
                JSONArray array = json.getJSONArray("geonames");
                for (int i = 0; i < Math.min(array.length(), 3); i++) {
                    landmarks.add(array.getJSONObject(i).getString("title"));
                }
            }
        } catch (Exception e) { landmarks.add("Landmarks unavailable"); }
        return landmarks;
    }

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