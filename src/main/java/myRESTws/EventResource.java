package myRESTws;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

/*
author:t0337312
*/

@Path("events")
public class EventResource {

    private static final String ENDPOINT = "https://t0337312.documents.azure.com:443/+";
    private static final String KEY = "RzFCEjiExm09uTtwYHQRlKrCsieH8FGkvhVNA9AIHhON7v2U1oUHjkzvBRXEdlJkmS1aMyI9HCeaACDb4RNFXQ==";
    private static final String DATABASE_NAME = "coursework";
    private static final String CONTAINER_NAME = "Events";

    private final CosmosContainer container;

    public EventResource() {
        CosmosClient client = new CosmosClientBuilder()
                .endpoint(ENDPOINT)
                .key(KEY)
                .buildClient();
        this.container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
    }

    /**
     * FEATURE A: Subscribe Student
     */
    @POST
    @Path("subscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> subscribeStudent(Map<String, Object> studentData) {
        studentData.put("subscription_date", new Date().toString());
        return studentData; 
    }

    /**
     * FEATURE B: Add New Event
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> addEvent(Map<String, Object> eventData) {
        if (eventData.containsKey("event_id")) {
            eventData.put("id", String.valueOf(eventData.get("event_id")));
        } else {
            String newId = UUID.randomUUID().toString();
            eventData.put("id", newId);
            eventData.put("event_id", newId);
        }
        eventData.putIfAbsent("attendees", new ArrayList<String>());
        container.createItem(eventData);
        return eventData;
    }

    /**
     * FEATURE C: Sophisticated Search
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> searchEvents(
            @QueryParam("type") String type,
            @QueryParam("location") String location,
            @QueryParam("date") String date) {

        StringBuilder sql = new StringBuilder("SELECT * FROM c WHERE 1=1");
        List<SqlParameter> params = new ArrayList<>();

        if (type != null && !type.isEmpty()) {
            sql.append(" AND c.type = @type");
            params.add(new SqlParameter("@type", type));
        }
        if (location != null && !location.isEmpty()) {
            sql.append(" AND CONTAINS(LOWER(c.location), LOWER(@loc))");
            params.add(new SqlParameter("@loc", location));
        }
        if (date != null && !date.isEmpty()) {
            sql.append(" AND c.date = @date");
            params.add(new SqlParameter("@date", date));
        }

        SqlQuerySpec querySpec = new SqlQuerySpec(sql.toString(), params);
        CosmosPagedIterable<Map> results = container.queryItems(querySpec, new CosmosQueryRequestOptions(), Map.class);

        List<Map> eventList = new ArrayList<>();
        results.forEach(eventList::add);
        return eventList;
    }

    /**
     * FEATURE D: Register for Event
     */
    @POST
    @Path("{event_id}/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> registerForEvent(@PathParam("event_id") String eventId, Map<String, String> body) {
        String studentId = body.get("student_id");
        PartitionKey pk = new PartitionKey(eventId);
        Map<String, Object> event = container.readItem(eventId, pk, Map.class).getItem();

        List<String> attendees = (List<String>) event.getOrDefault("attendees", new ArrayList<String>());
        int maxParticipants = Integer.parseInt(event.get("max_participants").toString());

        if (attendees.size() < maxParticipants && !attendees.contains(studentId)) {
            attendees.add(studentId);
            event.put("attendees", attendees);
            container.replaceItem(event, eventId, pk, new CosmosItemRequestOptions());
        }
        return event;
    }

    /**
     * FEATURE E: Detailed View with External API Integration
     * Logic: Fetch local data from Cosmos + external data from APIs.
     */
   /**
     * FEATURE E: Research and utilise external RESTful services
     * This endpoint combines internal NoSQL data with external Weather & Geolocation data.
     */
    @GET
    @Path("{event_id}/details")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getExtendedEventDetails(@PathParam("event_id") String eventId) {
        // 1. Fetch student event from Cosmos DB
        PartitionKey pk = new PartitionKey(eventId);
        Map<String, Object> event = container.readItem(eventId, pk, Map.class).getItem();

        // 2. Call External Services
        ExternalApiService external = new ExternalApiService();
        
        // 3. Add Weather for the city (Public Info)
        // Using "Nottingham" as default for your campus events
        event.put("current_weather", external.getWeather("Nottingham"));
        
        // 4. Add Landmarks (GeoNames POI)
        // Using coordinates for Nottingham: 52.95, -1.15
        event.put("nearby_landmarks", external.getLandmarks(52.95, -1.15));
        
        event.put("data_source_info", "Extended via OpenWeather and GeoNames APIs");

        return event;
    }
}