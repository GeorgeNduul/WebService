        package myRESTws;

import static com.azure.core.http.HttpHeaderName.FROM;
        import com.azure.cosmos.*;
        import com.azure.cosmos.models.*;
        import com.azure.cosmos.util.CosmosPagedIterable;
        import javax.ws.rs.*;
        import javax.ws.rs.core.MediaType;
        import java.util.*;

        /*
        author :t0337312
        */
        
        @Path("events")
        public class EventResource {

            // --- Configuration ---
            private static final String ENDPOINT = "https://t0337312.documents.azure.com:443/";
            private static final String KEY = "RzFCEjiExm09uTtwYHQRlKrCsieH8FGkvhVNA9AIHhON7v2U1oUHjkzvBRXEdlJkmS1aMyI9HCeaACDb4RNFXQ==";
            private static final String DATABASE_NAME = "coursework";
            private static final String CONTAINER_NAME = "Events";
            private final CosmosContainer container;
            private final ExternalApiService externalApi = new ExternalApiService(); // Integrated Service

            public EventResource() {
                // this is connecting the information in the database using the key and endpoint 
                CosmosClient client = new CosmosClientBuilder()
                        .endpoint(ENDPOINT)
                        .key(KEY)
                        .buildClient();
                this.container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
            }

            // --- Private Helper: Reduces Redundant Code ---
            private Map<String, Object> findEventById(String id) {
                try {
                    return container.readItem(id, new PartitionKey(id), Map.class).getItem();
                } catch (Exception e) {
                    return null;
                }
            }

            // --- Endpoints ---

            @POST
            @Consumes(MediaType.APPLICATION_JSON)
            @Produces(MediaType.APPLICATION_JSON)
            public Map<String, Object> addEvent(Map<String, Object> eventData) {
                String id = eventData.containsKey("event_id") ? 
                            String.valueOf(eventData.get("event_id")) : UUID.randomUUID().toString();

                eventData.put("id", id);
                eventData.put("event_id", id);
                eventData.putIfAbsent("attendees", new ArrayList<String>());
                eventData.putIfAbsent("attendance_list", new ArrayList<String>()); 
                eventData.putIfAbsent("ratings", new ArrayList<Map<String, Object>>());
                eventData.putIfAbsent("is_advertised", true); 

                container.createItem(eventData);
                return eventData;
            }

           @GET
@Path("search")
@Produces(MediaType.APPLICATION_JSON)
public List<Map> searchEvents(@QueryParam("query") String query) {
    List<Map> list = new ArrayList<>();
    
    // Handle empty search by returning all events
    if (query == null || query.trim().isEmpty()) {
        container.queryItems("SELECT * FROM c", new CosmosQueryRequestOptions(), Map.class)
                 .forEach(list::add);
        return list;
    }

    // 1. Define the sophisticated SQL query with case-insensitive partial matching
    String sql = "SELECT * FROM c WHERE " +
                 "CONTAINS(LOWER(c.title), LOWER(@searchQuery)) OR " +
                 "CONTAINS(LOWER(c.type), LOWER(@searchQuery)) OR " +
                 "CONTAINS(LOWER(c.location), LOWER(@searchQuery))";

    // 2. Use SqlQuerySpec to safely inject the parameter (Security Best Practice)
    SqlQuerySpec querySpec = new SqlQuerySpec(sql, 
        new SqlParameter("@searchQuery", query.trim())
    );

    // 3. Execute and collect results
    container.queryItems(querySpec, new CosmosQueryRequestOptions(), Map.class)
             .forEach(list::add);

    return list;
}
            @GET
            @Path("promoted")
            @Produces(MediaType.APPLICATION_JSON)
            public List<Map> getPromotedEvents() {
                String sql = "SELECT * FROM c WHERE c.is_advertised = true";
                CosmosPagedIterable<Map> results = container.queryItems(sql, new CosmosQueryRequestOptions(), Map.class);
                List<Map> list = new ArrayList<>();
                results.forEach(list::add);
                return list;
            }

            @POST
            @Path("{event_id}/register")
            @Produces(MediaType.APPLICATION_JSON)
            public Map<String, Object> register(@PathParam("event_id") String event_id, Map<String, String> body) {
                Map<String, Object> event = findEventById(event_id);
                if (event == null) return Collections.singletonMap("error", "Event ID not found.");

                List<String> attendees = (List<String>) event.getOrDefault("attendees", new ArrayList<String>());
                String sId = body.get("student_id");

                if (sId != null && !attendees.contains(sId)) {
                    attendees.add(sId);
                    event.put("attendees", attendees);
                    container.replaceItem(event, event_id, new PartitionKey(event_id), new CosmosItemRequestOptions());
                }
                return event;
            }

            @POST
            @Path("{event_id}/attendance")
            @Produces(MediaType.APPLICATION_JSON)
            public Map<String, Object> markAttendance(@PathParam("event_id") String eventId, Map<String, String> body) {
                Map<String, Object> event = findEventById(eventId);
                if (event == null) return Collections.singletonMap("error", "Event ID not found.");

                List<String> attendance = (List<String>) event.getOrDefault("attendance_list", new ArrayList<String>());
                String sId = body.get("student_id");

                if (sId != null && !attendance.contains(sId)) {
                    attendance.add(sId);
                    event.put("attendance_list", attendance);
                    container.replaceItem(event, eventId, new PartitionKey(eventId), new CosmosItemRequestOptions());
                }
                return event;
            }

            @POST
            @Path("{event_id}/rate")
            @Produces(MediaType.APPLICATION_JSON)
            public Map<String, Object> rateEvent(@PathParam("event_id") String eventId, Map<String, Object> ratingData) {
                Map<String, Object> event = findEventById(eventId);
                if (event == null) return Collections.singletonMap("error", "Event ID not found.");

                List<Map<String, Object>> ratings = (List<Map<String, Object>>) event.getOrDefault("ratings", new ArrayList<Map<String, Object>>());
                ratingData.put("rating_date", new java.util.Date().toString());
                ratings.add(ratingData);
                event.put("ratings", ratings);

                container.replaceItem(event, eventId, new PartitionKey(eventId), new CosmosItemRequestOptions());
                return event;
            }

            @GET
            @Path("{event_id}/details")
            @Produces(MediaType.APPLICATION_JSON)
            public Map<String, Object> getEventDetails(@PathParam("event_id") String eventId) {
                Map<String, Object> event = findEventById(eventId);
                if (event == null) return Collections.singletonMap("error", "Event ID not found.");

                // Dynamic Weather
                String city = (String) event.getOrDefault("location", "London");
                event.put("external_weather", externalApi.getWeather(city));
                
                String weatherDesc = (String) event.get( "external_weather");
                String advice = "Dress comfortably!"; // Default

                if (weatherDesc.toLowerCase().contains("rain")) {
                    advice = "☔ Bring an umbrella and waterproof shoes.";
                } else if (weatherDesc.toLowerCase().contains("snow")) {
                    advice = "❄️ Heavy winter gear required.";
                } else if (weatherDesc.toLowerCase().contains("clear") || weatherDesc.toLowerCase().contains("sun")) {
                    advice = "☀️ Great for outdoors! Don't forget sunglasses.";
                }

                event.put("recommendation_advice", advice);

                // Dynamic Landmarks using Latitude/Longitude from DB
                try {
                    if (event.containsKey("lat") && event.containsKey("lng")) {
                        double lat = Double.parseDouble(event.get("lat").toString());
                        double lng = Double.parseDouble(event.get("lng").toString());
                        event.put("nearby_attractions", externalApi.getLandmarks(lat, lng));
                    }
                } catch (Exception e) {
                    event.put("nearby_attractions", "Location data invalid");
                }

                return event;
            }
            @DELETE
    @Path("{event_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> deleteEvent(@PathParam("event_id") String eventId) {
        try {
            // PartitionKey must match the ID in your container setup
            container.deleteItem(eventId, new PartitionKey(eventId), new CosmosItemRequestOptions());
            return Collections.singletonMap("status", "Event " + eventId + " deleted successfully.");
        } catch (com.azure.cosmos.CosmosException e) {
            if (e.getStatusCode() == 404) {
                return Collections.singletonMap("error", "Event not found.");
            }
            return Collections.singletonMap("error", "Cosmos Error: " + e.getMessage());
        } catch (Exception e) {
            return Collections.singletonMap("error", "Server Error: " + e.getMessage());
        }
    }
        }