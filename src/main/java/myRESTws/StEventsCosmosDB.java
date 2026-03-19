///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package myRESTws;
//
///**
// *
// * @author t0337312
// */
//
//
//import com.azure.cosmos.*;
//import com.azure.cosmos.models.*;
//import com.azure.cosmos.util.CosmosPagedIterable;
//import com.azure.cosmos.models.SqlParameter;
//import com.azure.cosmos.models.SqlQuerySpec;
//import java.util.Arrays;
//import java.util.Iterator;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.util.Map;
//
//public class StEventsCosmosDB {
//
//    // Cosmos DB account settings
//    private static final String ENDPOINT = "https://t0337312.documents.azure.com:443/+";
//    private static final String KEY = "RzFCEjiExm09uTtwYHQRlKrCsieH8FGkvhVNA9AIHhON7v2U1oUHjkzvBRXEdlJkmS1aMyI9HCeaACDb4RNFXQ==";
//    private static final String DATABASE_NAME = "coursework";
//    private static final String CONTAINER_NAME = "Events";
//    private static final String PARTITION_KEY_FIELD = "event_id";
//
//    public static void main(String[] args) {
//
//        // Create the Cosmos DB client
//        CosmosClient client = new CosmosClientBuilder()
//                .endpoint(ENDPOINT)
//                .key(KEY)
//                .buildClient();
//        try {
//            // Access database and container
//            CosmosDatabase database = client.getDatabase(DATABASE_NAME);
//            CosmosContainer container = database.getContainer(CONTAINER_NAME);
//
//            // make event attendance free 
//            System.out.println("\n===== MAKING THE EVENT ENTRY FREE =====");
//            boolean updated = makeEventEntryFree(container, 2);
//
//            if (updated) {
//                System.out.println("Update successful.");
//            } else {
//                System.out.println("Update failed or event not found.");
//            }
//        } finally {
//            client.close();
//        }
//    }
//
//    // ----------------------------------------------------
//    // ALLOWING FREE ENTRY TO THE EVENT matching eventID
//    // ----------------------------------------------------
//    public static boolean makeEventEntryFree(CosmosContainer container, int event_id) {
//
//        // First, find the event
//        String sql = "SELECT * FROM c WHERE c.event_id = @event_id";
//
//        SqlQuerySpec querySpec = new SqlQuerySpec(
//                sql,
//                Arrays.asList(
//                        new SqlParameter("@event_id", event_id)
//                )
//        );
//
//        CosmosPagedIterable<Map> results =
//                container.queryItems(querySpec, new CosmosQueryRequestOptions(), Map.class);
//
//        Iterator<Map> iterator = results.iterator();
//
//        if (iterator.hasNext()) {
//            //changing the event cost to zero!
//            JSONObject event = new JSONObject(iterator.next());
//            
//            event.put("cost", 0);
//            
//            /*
//            * To replace an item in Cosmos DB, we need:
//            * 1. the updated JSON object
//            * 2. the partition key value of that item
//            */
//           Object pkValue = event.get(PARTITION_KEY_FIELD);
//           PartitionKey partitionKey = buildPartitionKey(pkValue);
//
//           container.replaceItem(
//                   event.toMap(),
//                   event.getString("id"),
//                   partitionKey,
//                   new CosmosItemRequestOptions()
//           );
//           return true;
//        }      
//        return false;
//
//    }
//
//    // ----------------------------------------------------
//    // HELPER METHOD
//    // Build a Cosmos PartitionKey from the field value
//    // ----------------------------------------------------
//    public static PartitionKey buildPartitionKey(Object value) {
//
//        if (value instanceof String) {
//            return new PartitionKey((String) value);
//        } else if (value instanceof Integer) {
//            return new PartitionKey((Integer) value);
//        } else if (value instanceof Long) {
//            return new PartitionKey((Long) value);
//        } else if (value instanceof Boolean) {
//            return new PartitionKey((Boolean) value);
//        } else if (value instanceof Double) {
//            return new PartitionKey((Double) value);
//        } else {
//            // Fallback: convert unknown types to string
//            return new PartitionKey(String.valueOf(value));
//        }
//    }
//}
