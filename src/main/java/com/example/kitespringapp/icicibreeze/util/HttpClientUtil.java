package com.example.kitespringapp.icicibreeze.util;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;
import org.apache.commons.codec.digest.DigestUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HttpClientUtil {
    
    public static String currentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        return sdf.format(new Date(System.currentTimeMillis())) + ".000Z";
    }

    public static String checksumValue(JSONObject jsonData, String timeStamp, String secretKey) throws JSONException {
        String hexData = timeStamp + jsonData.toString() + secretKey;
        System.out.println("Checksum Input: " + hexData); // Debug log
        String checksum = DigestUtils.sha256Hex(hexData);
        System.out.println("Generated Checksum: " + checksum); // Debug log
        return checksum;
    }

    private static String getBase64SessionToken(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            return "";
        }
        // If already base64 encoded, return as is
        if (sessionToken.matches("^[A-Za-z0-9+/=]+$")) {
            return sessionToken;
        }
        // Otherwise encode to base64
        return Base64.getEncoder().encodeToString(sessionToken.getBytes(StandardCharsets.UTF_8));
    }

    public static List<Header> generateHeaders(JSONObject requestBody, String apiKey, String secretKey) {
        try {
            String timestamp = currentTimestamp();
            String checksum = checksumValue(requestBody, timestamp, secretKey);
            
            List<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader("X-Checksum", "token "+checksum));
            headers.add(new BasicHeader("X-Timestamp", timestamp));

            // Log headers for debugging
            System.out.println("\nAuth Headers:");
            headers.forEach(header -> System.out.println(header.getName() + ": " + header.getValue()));
            
            return headers;
        } catch (JSONException e) {
            throw new RuntimeException("Error generating headers: " + e.getMessage(), e);
        }
    }

    public static JSONObject makeRequest(String url, JSONObject requestBody, String apiKey, String secretKey, String sessionToken, String method) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Create request based on method
            HttpEntityEnclosingRequestBase request;
            switch (method.toUpperCase()) {
                case "GET":
                    request = new HttpGetWithEntity(url);
                    break;
                case "POST":
                    request = new HttpPostWithEntity(url);
                    break;
                case "PUT":
                    request = new HttpPutWithEntity(url);
                    break;
                case "DELETE":
                    request = new HttpDeleteWithEntity(url);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }
            
            // Set headers exactly as per SDK
            request.setHeader("Content-type", "application/json");
            
            // Set auth headers (X-Checksum and X-Timestamp)
            List<Header> authHeaders = generateHeaders(requestBody, apiKey, secretKey);
            request.setHeader("X-Checksum", authHeaders.get(0).getValue());
            request.setHeader("X-Timestamp", authHeaders.get(1).getValue());
            
            // Set app key and session token
            request.setHeader("X-AppKey", apiKey);
            
            if (!sessionToken.isEmpty()) {
                request.setHeader("X-SessionToken", sessionToken);
            }
            
            // Set body
            request.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            // Log complete request details
            System.out.println("\nComplete Request Headers:");
            for (Header header : request.getAllHeaders()) {
                System.out.println(header.getName() + ": " + header.getValue());
            }
            System.out.println("Request Body: " + requestBody.toString() + "\n");

            // Execute request
            org.apache.http.HttpResponse response = client.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            
            // Check for error response
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new IOException("API Error: " + responseBody);
            }
            
            return new JSONObject(responseBody);
        }
    }

    // Convenience methods for different HTTP methods
    public static JSONObject get(String url, JSONObject requestBody, String apiKey, String sessionToken, String secretKey) throws IOException {
        return makeRequest(url, requestBody, apiKey, secretKey, sessionToken, "GET");
    }

    public static JSONObject post(String url, JSONObject requestBody, String apiKey, String sessionToken, String secretKey) throws IOException {
        return makeRequest(url, requestBody, apiKey, secretKey, sessionToken, "POST");
    }

    public static JSONObject put(String url, JSONObject requestBody, String apiKey,String sessionToken, String secretKey) throws IOException {
        return makeRequest(url, requestBody, apiKey, secretKey, sessionToken, "PUT");
    }

    public static JSONObject delete(String url, JSONObject requestBody, String apiKey, String sessionToken, String secretKey) throws IOException {
        return makeRequest(url, requestBody, apiKey, secretKey, sessionToken, "DELETE");
    }
} 