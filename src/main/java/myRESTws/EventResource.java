package myRESTws;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("event")
public class EventResource {

    private final String FILE_PATH = "C:\\Users\\t0337312\\OneDrive - Nottingham Trent University\\Documents\\NetBeansProjects\\CWK\\student_meetup.json";

    public EventResource() {
    }

    /**
     * Requirement (a): Subscribe to the system
     * Usage: /event/subscribe?studentId=S123
     */
    @GET
    @Path("subscribe")
    @Produces("application/json")
    public String subscribe(@QueryParam("studentId") String studentId) {
        try {
            String content = Files.readString(Paths.get(FILE_PATH), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(content);
            
            // Ensure a 'students' array exists in your JSON
            if (!root.has("students")) {
                root.put("students", new JSONArray());
            }
            
            JSONArray students = root.getJSONArray("students");
            students.put(studentId);
            
            Files.writeString(Paths.get(FILE_PATH), root.toString(4), StandardCharsets.UTF_8);
            return root.toString();
        } catch (Exception e) {
            System.out.println("Error in subscribe: " + e);
            return "something went wrong;; debug the RESTful webservice";
        }
    }

    /**
     * Requirement (c): List and Search for events
     */
    @GET
    @Path("search")
    @Produces("application/json")
    public String searchEvents(@QueryParam("type") String type, @QueryParam("location") String location) {
        try {
            String content = Files.readString(Paths.get(FILE_PATH), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(content);
            JSONArray allEvents = root.getJSONArray("student_meetup");
            JSONArray filteredResults = new JSONArray();

            for (int i = 0; i < allEvents.length(); i++) {
                JSONObject event = allEvents.getJSONObject(i);
                boolean matches = true;

                if (type != null && !event.getString("type").equalsIgnoreCase(type)) matches = false;
                if (location != null && !event.getString("location").toLowerCase().contains(location.toLowerCase())) matches = false;

                if (matches) filteredResults.put(event);
            }
            return filteredResults.toString();
        } catch (Exception e) {
            System.out.println("Error in search: " + e);
            return "something went wrong;; debug the RESTful webservice";
        }
    }

    /**
     * Requirement (b): Add new events
     */
    @GET
    @Path("add")
    @Produces("application/json")
    public String addEvent(
            @QueryParam("pubId") String pubId, 
            @QueryParam("title") String title,
            @QueryParam("type") String type,
            @QueryParam("loc") String loc,
            @QueryParam("max") int max) {
        try {
            String content = Files.readString(Paths.get(FILE_PATH), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(content);
            JSONArray meetups = root.getJSONArray("student_meetup");
            
            JSONObject newEvent = new JSONObject();
            newEvent.put("publisher_id", pubId);
            newEvent.put("title", title);
            newEvent.put("type", type);
            newEvent.put("location", loc);
            newEvent.put("max_participants", max);
            newEvent.put("attendees", new JSONArray()); 
            
            meetups.put(newEvent);
            Files.writeString(Paths.get(FILE_PATH), root.toString(4), StandardCharsets.UTF_8);
            
            return root.toString();
        } catch (Exception e) {
            System.out.println("Error in add: " + e);
            return "something went wrong;; debug the RESTful webservice";
        }
    }

    /**
     * Requirement (d): Register/Book an event
     */
    @GET
    @Path("book")
    @Produces("application/json")
    public String bookEvent(@QueryParam("eventTitle") String title, @QueryParam("studentId") String studentId) {
        try {
            String content = Files.readString(Paths.get(FILE_PATH), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(content);
            JSONArray meetups = root.getJSONArray("student_meetup");

            for (int i = 0; i < meetups.length(); i++) {
                JSONObject event = meetups.getJSONObject(i);
                if (event.getString("title").equalsIgnoreCase(title)) {
                    JSONArray attendees = event.getJSONArray("attendees");
                    
                    if (attendees.length() < event.getInt("max_participants")) {
                        attendees.put(studentId);
                    } else {
                        return "{\"error\": \"Event full\"}";
                    }
                    break;
                }
            }

            Files.writeString(Paths.get(FILE_PATH), root.toString(4), StandardCharsets.UTF_8);
            return root.toString();
        } catch (Exception e) {
            System.out.println("Error in book: " + e);
            return "something went wrong;; debug the RESTful webservice";
        }
    }
}