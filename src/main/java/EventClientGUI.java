import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * author t0337312
 */
public class EventClientGUI extends JFrame {
    private final String BASE_URL = "http://localhost:8080/CWKMAVEN-1.0-SNAPSHOT/api/events";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private JTextField titleF, pubF, dateF, costF, typeF, maxF, locF, latF, lngF;
    private JTextField targetIdF, studentIdF, scoreF, commentF, searchF;
    private JTextArea displayArea;

    public EventClientGUI() {
        setTitle("Campus Event Management System");
        setSize(1000, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- Left Panel Setup ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(450, 850));

        // 1. Search Panel
        JPanel searchP = new JPanel(new BorderLayout(5, 5));
        searchP.setBorder(new TitledBorder("Find Events"));
        searchF = new JTextField();
        JButton searchBtn = new JButton("Search by Title/Type");
        searchBtn.addActionListener(e -> handleSearch()); // Logic moved to method
        
        searchP.add(searchF, BorderLayout.CENTER);
        searchP.add(searchBtn, BorderLayout.EAST);

        // 2. Create Event Panel
        JPanel createP = new JPanel(new GridLayout(0, 2, 5, 5));
        createP.setBorder(new TitledBorder("Create New Event"));
        titleF = new JTextField(); createP.add(new JLabel("Title:")); createP.add(titleF);
        pubF = new JTextField("STUDENT001"); createP.add(new JLabel("Publisher ID:")); createP.add(pubF);
        dateF = new JTextField("2026-03-20"); createP.add(new JLabel("Date (YYYY-MM-DD):")); createP.add(dateF);
        costF = new JTextField("0"); createP.add(new JLabel("Cost:")); createP.add(costF);
        typeF = new JTextField("sport"); createP.add(new JLabel("Type:")); createP.add(typeF);
        maxF = new JTextField("10"); createP.add(new JLabel("Max Participants:")); createP.add(maxF);
        locF = new JTextField(); createP.add(new JLabel("Location (City):")); createP.add(locF);
        latF = new JTextField("51.5074"); createP.add(new JLabel("Latitude:")); createP.add(latF);
        lngF = new JTextField("-0.1278"); createP.add(new JLabel("Longitude:")); createP.add(lngF);

        JButton addBtn = new JButton("Post & Advertise");
        addBtn.setBackground(new Color(70, 130, 180));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> addEvent());
        
        JButton clearBtn = new JButton("Reset Form");
        clearBtn.addActionListener(e -> clearFields());
        
        createP.add(clearBtn); 
        createP.add(addBtn);

        // 3. Session Panel
        JPanel sessionP = new JPanel(new GridLayout(0, 2, 5, 5));
        sessionP.setBorder(new TitledBorder("User Session & Event ID"));
        targetIdF = new JTextField(); sessionP.add(new JLabel("Target Event ID:")); sessionP.add(targetIdF);
        studentIdF = new JTextField("S101"); sessionP.add(new JLabel("Your Student ID:")); sessionP.add(studentIdF);

        // 4. Actions Panel
        JPanel actionP = new JPanel(new GridLayout(2, 3, 5, 5));
        actionP.setBorder(new TitledBorder("Quick Actions"));
        
        JButton regBtn = new JButton("Register"); regBtn.addActionListener(e -> performAction("register"));
        JButton attBtn = new JButton("Mark Present"); attBtn.addActionListener(e -> performAction("attendance"));
        JButton adsBtn = new JButton("View Ads"); adsBtn.addActionListener(e -> sendRequest(BASE_URL + "/promoted", "GET", null));
        JButton detBtn = new JButton("Get Detailed Info"); detBtn.addActionListener(e -> getDetailedInfo());
        JButton mapBtn = new JButton("📍 View on Map"); mapBtn.addActionListener(e -> openInMap());
        JButton delBtn = new JButton("Delete Event");
        delBtn.setBackground(new Color(220, 20, 60)); delBtn.setForeground(Color.WHITE);
        delBtn.addActionListener(e -> deleteEvent());

        actionP.add(regBtn); actionP.add(attBtn); actionP.add(adsBtn);
        actionP.add(detBtn); actionP.add(mapBtn); actionP.add(delBtn);

        // 5. Rate Panel
        JPanel rateP = new JPanel(new GridLayout(0, 2, 5, 5));
        rateP.setBorder(new TitledBorder("Rate Finished Event"));
        scoreF = new JTextField(); rateP.add(new JLabel("Score (1-5):")); rateP.add(scoreF);
        commentF = new JTextField(); rateP.add(new JLabel("Comment:")); rateP.add(commentF);
        JButton rateBtn = new JButton("Submit Review"); rateBtn.addActionListener(e -> submitRating());
        rateP.add(new JLabel("")); rateP.add(rateBtn);

        leftPanel.add(searchP); leftPanel.add(createP); leftPanel.add(sessionP); leftPanel.add(actionP); leftPanel.add(rateP);
        add(leftPanel, BorderLayout.WEST);

        // --- Center Display ---
        displayArea = new JTextArea();
        displayArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        displayArea.setEditable(false);
        displayArea.setLineWrap(true);
        displayArea.setWrapStyleWord(true);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);
    }

    // --- Logic Methods ---

    private void handleSearch() {
        String query = searchF.getText().trim();
        if (query.isEmpty()) {
            sendRequest(BASE_URL, "GET", null);
        } else {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            sendRequest(BASE_URL + "/search?query=" + encoded, "GET", null);
        }
    }

    private void addEvent() {
        try {
            JSONObject json = new JSONObject();
            json.put("title", titleF.getText().trim());
            json.put("publisher_id", pubF.getText().trim());
            json.put("date", dateF.getText().trim());
            json.put("type", typeF.getText().trim());
            json.put("location", locF.getText().trim());
            json.put("lat", Double.parseDouble(latF.getText().trim()));
            json.put("lng", Double.parseDouble(lngF.getText().trim()));
            json.put("cost", Integer.parseInt(costF.getText().replaceAll("[^0-9]", "0")));
            json.put("max_participants", Integer.parseInt(maxF.getText().replaceAll("[^0-9]", "0")));
            json.put("is_advertised", true);

            sendRequest(BASE_URL, "POST", json.toString());
        } catch (Exception e) {
            displayArea.setText("Input Error: Check numbers and coordinates.");
        }
    }

    private void performAction(String action) {
        String id = targetIdF.getText().trim();
        if (id.isEmpty()) { displayArea.setText("Error: Need Event ID"); return; }
        JSONObject json = new JSONObject();
        json.put("student_id", studentIdF.getText().trim());
        sendRequest(BASE_URL + "/" + id + "/" + action, "POST", json.toString());
    }

    private void getDetailedInfo() {
        String id = targetIdF.getText().trim();
        if (id.isEmpty()) { displayArea.setText("Error: Enter Event ID in Session Panel"); return; }
        sendRequest(BASE_URL + "/" + id + "/details", "GET", null);
    }

    private void submitRating() {
    String id = targetIdF.getText().trim();
    if (id.isEmpty()) {
        displayArea.setText("⚠️ Error: Please enter a Target Event ID first.");
        return;
    }

    try {
        // 1. Sanitize the input to get only numbers
        String scoreStr = scoreF.getText().replaceAll("[^0-9]", "");
        int score = scoreStr.isEmpty() ? 0 : Integer.parseInt(scoreStr);

        // 2. RANGE VALIDATION (The 1-5 Rule)
        if (score < 1 || score > 5) {
            displayArea.setText("⛔ Invalid Score: Rating must be between 1 and 5.");
            displayArea.setForeground(Color.RED);
            return; // Stop the method here so the request is never sent
        }

        // 3. If valid, build the JSON and send the request
        JSONObject json = new JSONObject();
        json.put("student_id", studentIdF.getText().trim());
        json.put("comment", commentF.getText().trim());
        json.put("score", score);

        sendRequest(BASE_URL + "/" + id + "/rate", "POST", json.toString());
        
    } catch (Exception e) {
        displayArea.setText("❌ Error building rating: " + e.getMessage());
    }
}

    private void deleteEvent() {
        String id = targetIdF.getText().trim();
        if (id.isEmpty()) { displayArea.setText("Error: Need Event ID to delete."); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete event " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            sendRequest(BASE_URL + "/" + id, "DELETE", null);
        }
    }

    private void openInMap() {
        try {
            String lat = latF.getText().trim();
            String lng = lngF.getText().trim();
            String url = "http://maps.google.com/?q=" + lat + "," + lng;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            displayArea.setText("Error opening map: " + ex.getMessage());
        }
    }

    private void clearFields() {
        titleF.setText(""); locF.setText(""); targetIdF.setText("");
        displayArea.setText("Form Reset.");
    }

    private void sendRequest(String url, String method, String jsonBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        
        if (method.equals("POST")) {
            builder.header("Content-Type", "application/json")
                   .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else if (method.equals("DELETE")) {
            builder.DELETE();
        } else {
            builder.GET();
        }

        httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenAccept(body -> SwingUtilities.invokeLater(() -> {
                try {
                    if (body.contains("external_weather")) {
                        JSONObject obj = new JSONObject(body);
                        StringBuilder sb = new StringBuilder("=== TRAVEL REPORT ===\n\n");
                        sb.append("EVENT:      ").append(obj.optString("title")).append("\n");
                        sb.append("LOCATION:   ").append(obj.optString("location")).append("\n");
                        sb.append("WEATHER:    ").append(obj.optString("external_weather")).append("\n");
                        sb.append("ADVICE:     ").append(obj.optString("recommendation_advice")).append("\n");
                        sb.append("LANDMARKS:  \n");
                        JSONArray arr = obj.optJSONArray("nearby_attractions");
                        if (arr != null) {
                            for (int i = 0; i < arr.length(); i++) {
                                sb.append("  • ").append(arr.getString(i)).append("\n");
                            }
                        }
                        displayArea.setText(sb.toString());
                        displayArea.setForeground(new Color(0, 102, 51));
                    } else if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                        displayArea.setText(body.trim().startsWith("{") ? 
                            new JSONObject(body).toString(4) : new JSONArray(body).toString(4));
                        displayArea.setForeground(Color.BLACK);
                    } else {
                        displayArea.setText(body);
                        displayArea.setForeground(Color.BLACK);
                    }
                } catch (Exception e) {
                    displayArea.setText(body);
                    displayArea.setForeground(Color.RED);
                }
            }));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EventClientGUI().setVisible(true));
    }
}