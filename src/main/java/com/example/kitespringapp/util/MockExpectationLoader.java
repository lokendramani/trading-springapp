package com.example.kitespringapp.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.HttpURLConnection;
import java.net.URL;

public class MockExpectationLoader {

    private static final String MOCKSERVER_URL = "http://localhost:1080/mockserver/expectation";

    public static void main(String[] args) {
        String dir = "D:\\Manidata\\DevWork\\mock-response\\";
        String configFileName = dir + "ConfigurationFile.txt";
        File configFile = new File(configFileName);

        if (!configFile.exists()) {
            System.err.println("ConfigurationFile.txt not found.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) continue;

                // Split the line by "-" to get all components
                String[] parts = line.split("-");
                if (parts.length < 3) {
                    System.err.println("Invalid line format: " + line);
                    continue;
                }

                String responseFileName = parts[0];
                String path = parts[1];
                String method = parts[2];

                // Read the response JSON file
                String responseBody = new String(Files.readAllBytes(Paths.get(dir + responseFileName)));

                // Create the basic request structure
                JSONObject expectation = new JSONObject();
                JSONObject httpRequest = new JSONObject();
                httpRequest.put("method", method);
                httpRequest.put("path", path);

                // Handle POST body or GET query parameters based on method
                if (method.equals("POST") && parts.length >= 4) {
                    // Handle POST body
                    String body = parts[3];
                    if (!body.isEmpty()) {
                        httpRequest.put("body", new JSONObject()
                                .put("type", "STRING")
                                .put("string", body));
                    }
                } else if (method.equals("GET") && parts.length >= 4) {
                    // Handle GET query parameters
                    String queryString = parts[3];
                    System.out.println("Processing query parameters: " + queryString);
                    
                    // Split query string by & to get individual parameters
                    String[] queryPairs = queryString.split("&");
                    JSONObject queryParamJson = new JSONObject();
                    
                    // Group parameters by name
                    Map<String, List<String>> paramMap = new HashMap<>();
                    for (String pair : queryPairs) {
                        String[] keyValue = pair.split("=");
                        if (keyValue.length == 2) {
                            String key = keyValue[0];
                            String value = keyValue[1];
                            paramMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                        }
                    }
                    
                    // Convert grouped parameters to JSON format
                    for (Map.Entry<String, List<String>> entry : paramMap.entrySet()) {
                        JSONArray values = new JSONArray();
                        entry.getValue().forEach(values::put);
                        queryParamJson.put(entry.getKey(), values);
                    }
                    
                    if (!queryParamJson.isEmpty()) {
                        httpRequest.put("queryStringParameters", queryParamJson);
                    }
                    System.out.println("Query parameters JSON: " + queryParamJson.toString(2));
                }

                // Create the complete expectation
                JSONObject httpResponse = new JSONObject();
                httpResponse.put("statusCode", 200);
                httpResponse.put("headers", new JSONObject().put("Content-Type", new JSONArray().put("application/json")));
                httpResponse.put("body", responseBody);

                expectation.put("httpRequest", httpRequest);
                expectation.put("httpResponse", httpResponse);
                expectation.put("times", new JSONObject().put("unlimited", true));

                sendExpectation(expectation.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendExpectation(String expectationJson) {
        try {
            URL url = new URL(MOCKSERVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(expectationJson.getBytes());
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 201) {
                System.out.println("Expectation created successfully.");
            } else {
                System.out.println("Failed to create expectation: HTTP " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("Error sending expectation to MockServer.");
            e.printStackTrace();
        }
    }
}
