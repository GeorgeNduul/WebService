        import javax.swing.*;
        import javax.swing.border.TitledBorder;
        import java.awt.*;
        import java.net.URI;
        import java.net.http.*;

        public class EventClientGUI extends JFrame {
            private final String BASE_URL = "http://localhost:8080/CWKMAVEN-1.0-SNAPSHOT/api/events";
            private final HttpClient httpClient = HttpClient.newHttpClient();

            private JTextField titleF, pubF, dateF, costF, typeF, maxF, locF;
            private JTextField targetIdF, studentIdF, scoreF, commentF, searchF;
            private JTextArea displayArea;

            public EventClientGUI() {
                setTitle("Campus Event Management System");
                setSize(900, 850);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setLayout(new BorderLayout(10, 10));

                JPanel leftPanel = new JPanel();
                leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
                leftPanel.setPreferredSize(new Dimension(420, 850));

                JPanel searchP = new JPanel(new BorderLayout(5, 5));
                searchP.setBorder(new TitledBorder("Find Events"));
                searchF = new JTextField();
                JButton searchBtn = new JButton("Search by Title/Type");
                searchBtn.addActionListener(e -> sendRequest(BASE_URL + "/search?query=" + searchF.getText().trim().replace(" ", "%20"), "GET", null));
                searchP.add(searchF, BorderLayout.CENTER);
                searchP.add(searchBtn, BorderLayout.EAST);

                JPanel createP = new JPanel(new GridLayout(0, 2, 5, 5));
                createP.setBorder(new TitledBorder("Create New Event"));
                titleF = new JTextField(); createP.add(new JLabel("Title:")); createP.add(titleF);
                pubF = new JTextField("STUDENT001"); createP.add(new JLabel("Publisher ID:")); createP.add(pubF);
                dateF = new JTextField("2026-03-20"); createP.add(new JLabel("Date:")); createP.add(dateF);
                costF = new JTextField("0"); createP.add(new JLabel("Cost:")); createP.add(costF);
                typeF = new JTextField("sport"); createP.add(new JLabel("Type:")); createP.add(typeF);
                maxF = new JTextField("10"); createP.add(new JLabel("Max Participants:")); createP.add(maxF);
                locF = new JTextField(); createP.add(new JLabel("Location:")); createP.add(locF);
                JButton addBtn = new JButton("Post & Advertise");
                addBtn.setBackground(new Color(70, 130, 180));
                addBtn.setForeground(Color.WHITE);
                addBtn.addActionListener(e -> addEvent());
                createP.add(new JLabel("")); createP.add(addBtn);

                JPanel sessionP = new JPanel(new GridLayout(0, 2, 5, 5));
                sessionP.setBorder(new TitledBorder("User Session"));
                targetIdF = new JTextField(); sessionP.add(new JLabel("Target Event ID:")); sessionP.add(targetIdF);
                studentIdF = new JTextField("S101"); sessionP.add(new JLabel("Your Student ID:")); sessionP.add(studentIdF);

                JPanel actionP = new JPanel(new GridLayout(1, 3, 5, 5));
                actionP.setBorder(new TitledBorder("Quick Actions"));
                JButton regBtn = new JButton("Register"); regBtn.addActionListener(e -> performAction("register"));
                JButton attBtn = new JButton("Mark Present"); attBtn.addActionListener(e -> performAction("attendance"));
                JButton adsBtn = new JButton("View Ads"); adsBtn.addActionListener(e -> sendRequest(BASE_URL + "/promoted", "GET", null));
                actionP.add(regBtn); actionP.add(attBtn); actionP.add(adsBtn);

                JPanel rateP = new JPanel(new GridLayout(0, 2, 5, 5));
                rateP.setBorder(new TitledBorder("Rate Finished Event"));
                scoreF = new JTextField(); rateP.add(new JLabel("Score (1-5):")); rateP.add(scoreF);
                commentF = new JTextField(); rateP.add(new JLabel("Comment:")); rateP.add(commentF);
                JButton rateBtn = new JButton("Submit Review"); rateBtn.addActionListener(e -> submitRating());
                rateP.add(new JLabel("")); rateP.add(rateBtn);

                leftPanel.add(searchP); leftPanel.add(createP); leftPanel.add(sessionP); leftPanel.add(actionP); leftPanel.add(rateP);
                add(leftPanel, BorderLayout.WEST);

                displayArea = new JTextArea();
                displayArea.setEditable(false);
                displayArea.setBackground(new Color(240, 240, 240));
                add(new JScrollPane(displayArea), BorderLayout.CENTER);
            }

            private void addEvent() {
                String cost = costF.getText().replaceAll("[^0-9]", "");
                String max = maxF.getText().replaceAll("[^0-9]", "");
                String json = String.format(
                    "{\"title\":\"%s\", \"publisher_id\":\"%s\", \"date\":\"%s\", \"cost\":%s, \"type\":\"%s\", \"max_participants\":%s, \"location\":\"%s\", \"is_advertised\":true}",
                    titleF.getText(), pubF.getText(), dateF.getText(), cost.isEmpty()?"0":cost, typeF.getText(), max.isEmpty()?"0":max, locF.getText()
                );
                sendRequest(BASE_URL, "POST", json);
            }

            private void performAction(String action) {
                if (targetIdF.getText().trim().isEmpty()) { displayArea.setText("Error: Need Event ID"); return; }
                String json = String.format("{\"student_id\":\"%s\"}", studentIdF.getText().trim());
                sendRequest(BASE_URL + "/" + targetIdF.getText().trim() + "/" + action, "POST", json);
            }

            private void submitRating() {
                if (targetIdF.getText().trim().isEmpty()) { displayArea.setText("Error: Need Event ID"); return; }
                String score = scoreF.getText().replaceAll("[^0-9]", "");
                String json = String.format("{\"student_id\":\"%s\", \"score\": %s, \"comment\": \"%s\"}",
                        studentIdF.getText().trim(), score.isEmpty()?"5":score, commentF.getText());
                sendRequest(BASE_URL + "/" + targetIdF.getText().trim() + "/rate", "POST", json);
            }

       private void sendRequest(String url, String method, String jsonBody) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
        if (method.equals("POST")) {
            builder.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            builder.GET();
        }

        httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> SwingUtilities.invokeLater(() -> {
                    // Formatting logic for Vertical Output
                    String formatted = body.replace("{", "{\n  ")
                                           .replace("}", "\n}")
                                           .replace("\",\"", "\",\n  ")
                                           .replace("\":", "\": ")
                                           .replace("[", "[\n    ")
                                           .replace("]", "\n  ]");
                    displayArea.setText(formatted);
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> displayArea.setText("Error: " + ex.getMessage()));
                    return null;
                });
    }

            public static void main(String[] args) {
                SwingUtilities.invokeLater(() -> new EventClientGUI().setVisible(true));
            }
        }