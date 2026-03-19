///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package myRESTws;
//
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import org.json.JSONException;
//
//
//
///**
// *
// * @author t0337312
// */
//public class ReadStudentMeetup {
//    public static void main(String[] args) {
//
//        // Specify the path to the JSON file; modify if the file name or directory differs
//        String filePath = "\"C:\\Users\\t0337312\\OneDrive - Nottingham Trent University\\Documents\\NetBeansProjects\\CWK\\student_meetup.json\"";
//
//        try {
//            // Read the entire contents of the file into a String using UTF-8 encoding
//            String jsonText = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
//
//            // Convert the text into a JSONObject and access the 'student_meetup' array
//            JSONObject root = new JSONObject(jsonText);
//            JSONArray meetups = root.getJSONArray("student_meetup");
//
//            // Iterate through the array and output each object's key–value pairs
//            for (int i = 0; i < meetups.length(); i++) {
//                JSONObject event = meetups.getJSONObject(i);
//                //display the event title and organier
//                System.out.println("Event #" + (i + 1) + event.getString("title") +
//                        "organised by: " + event.getString("organiser_id"));
//
//                // Add a blank line for clearer separation between events
//                System.out.println();
//            }
//
//        } catch (IOException e) {
//            System.err.println("Unable to read the file: " + e.getMessage());
//        } catch (JSONException e) {
//            System.err.println("An error occurred while processing the JSON data: " + e.getMessage());
//        }
//    }
//}