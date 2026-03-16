package myRESTws;


/**
 *
 * @author t0337312
 */


import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.*;
import org.json.JSONObject;



@Path("events")
public class EventResource {

    private static final String ENDPOINT = "https://t0337312.documents.azure.com:443/+";
    private static final String KEY = "RzFCEjiExm09uTtwYHQRlKrCsieH8FGkvhVNA9AIHhON7v2U1oUHjkzvBRXEdlJkmS1aMyI9HCeaACDb4RNFXQ==";
    private static final String DATABASE_NAME = "coursework";
    private static final String CONTAINER_NAME = "Events";
   private static final String PARTITION_KEY_FIELD = "event_id";

    private final CosmosContainer container;

    public EventResource() {
        CosmosClient client = new CosmosClientBuilder()
                .endpoint(ENDPOINT)
                .key(KEY)
                .buildClient();
        this.container = client.getDatabase(DATABASE_NAME).getContainer(CONTAINER_NAME);
    }

    /**
     * FEATURE A: Subscribe/Register a Student
     * For a NoSQL approach, we can store students in a "Students" container 
     * or simply acknowledge their ID for future publishing/booking.
     */
    @POST
    @Path("subscribe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> subscribeStudent(Map<String, Object> studentData) {
        // Implementation logic for adding a student to a student container
        // Returns the student object directly
        return studentData; 
    }

    /**
     * FEATURE B: Add New Event
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> addEvent(Map<String, Object> eventData) {
        // Ensure the mandatory Cosmos 'id' field is set
        if (!eventData.containsKey("id")) {
            eventData.put("id", String.valueOf(eventData.get("event_id")));
        }
        
        // Initialize attendees if not provided
        eventData.putIfAbsent("attendees", new ArrayList<String>());

        container.createItem(eventData);
        return eventData;
    }

    /**
     * FEATURE C: List and Search Events
     * Dynamic filtering based on type, location, or cost.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> searchEvents(
            @QueryParam("type") String type,
            @QueryParam("location") String location,
            @QueryParam("date") String date) {

        StringBuilder sql = new StringBuilder("SELECT * FROM c WHERE 1=1");
        List<SqlParameter> params = new ArrayList<>();

        if (type != null) {
            sql.append(" AND c.type = @type");
            params.add(new SqlParameter("@type", type));
        }
        if (location != null) {
            sql.append(" AND CONTAINS(c.location, @loc)");
            params.add(new SqlParameter("@loc", location));
        }
        if (date != null) {
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
     * FEATURE D: Register for an Event
     * Updates the attendees list for a specific event.
     */
    @POST
    @Path("{event_id}/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> registerForEvent(@PathParam("event_id") String eventId, Map<String, String> body) {
        String studentId = body.get("student_id");
        
        // Fetch existing event
        PartitionKey pk = new PartitionKey(Integer.parseInt(eventId));
        Map<String, Object> event = container.readItem(eventId, pk, Map.class).getItem();

        List<String> attendees = (List<String>) event.get("attendees");
        int maxParticipants = (int) event.get("max_participants");

        // Simple validation logic
        if (attendees.size() < maxParticipants && !attendees.contains(studentId)) {
            attendees.add(studentId);
            event.put("attendees", attendees);
            
            // Persist the update
            container.replaceItem(event, eventId, pk, new CosmosItemRequestOptions());
        }

        return event;
    }
}