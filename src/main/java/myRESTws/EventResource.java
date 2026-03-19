package myRESTws;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("events")
public class EventResource {

    private static final String ENDPOINT = "https://t0337312.documents.azure.com:443/";
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
        String sql = "SELECT * FROM c WHERE CONTAINS(LOWER(c.title), LOWER('" + query + "')) OR LOWER(c.type) = LOWER('" + query + "')";
        CosmosPagedIterable<Map> results = container.queryItems(sql, new CosmosQueryRequestOptions(), Map.class);
        List<Map> list = new ArrayList<>();
        results.forEach(list::add);
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
    try {
        // IMPORTANT: Ensure your PartitionKey matches the ID
        PartitionKey pk = new PartitionKey(event_id);
        
        // Use readItem but check if it actually exists
        CosmosItemResponse<Map> response = container.readItem(event_id, pk, Map.class);
        Map<String, Object> event = response.getItem();

        if (event == null) {
            return Collections.singletonMap("error", "Event ID not found in database.");
        }

        List<String> attendees = (List<String>) event.getOrDefault("attendees", new ArrayList<String>());
        String sId = body.get("student_id");

        if (sId != null && !attendees.contains(sId)) {
            attendees.add(sId);
            event.put("attendees", attendees);
            container.replaceItem(event, event_id, pk, new CosmosItemRequestOptions());
        }
        return event;

    } catch (com.azure.cosmos.CosmosException ce) {
        // If the ID isn't found, Cosmos throws an exception. We catch it here.
        return Collections.singletonMap("error", "CosmosDB Error: Item likely does not exist.");
    } catch (Exception e) {
        return Collections.singletonMap("error", "Server Error: " + e.getMessage());
    }
}

    @POST
    @Path("{event_id}/attendance")
    public Map<String, Object> markAttendance(@PathParam("event_id") String eventId, Map<String, String> body) {
        try {
            PartitionKey pk = new PartitionKey(eventId);
            Map<String, Object> event = container.readItem(eventId, pk, Map.class).getItem();
            List<String> attendance = (List<String>) event.getOrDefault("attendance_list", new ArrayList<String>());
            String sId = body.get("student_id");
            if (sId != null && !attendance.contains(sId)) {
                attendance.add(sId);
                event.put("attendance_list", attendance);
                container.replaceItem(event, eventId, pk, new CosmosItemRequestOptions());
            }
            return event;
        } catch (Exception e) {
            return Collections.singletonMap("error", "Event Not Found");
        }
    }

    @POST
    @Path("{event_id}/rate")
    public Map<String, Object> rateEvent(@PathParam("event_id") String eventId, Map<String, Object> ratingData) {
        try {
            PartitionKey pk = new PartitionKey(eventId);
            Map<String, Object> event = container.readItem(eventId, pk, Map.class).getItem();
            List<Map<String, Object>> ratings = (List<Map<String, Object>>) event.getOrDefault("ratings", new ArrayList<Map<String, Object>>());
            ratingData.put("rating_date", new java.util.Date().toString());
            ratings.add(ratingData);
            event.put("ratings", ratings);
            container.replaceItem(event, eventId, pk, new CosmosItemRequestOptions());
            return event;
        } catch (Exception e) {
            return Collections.singletonMap("error", "Event Not Found");
        }
    }
}