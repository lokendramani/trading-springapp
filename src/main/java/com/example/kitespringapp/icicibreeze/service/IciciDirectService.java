package com.example.kitespringapp.icicibreeze.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.example.kitespringapp.icicibreeze.util.HttpClientUtil;

@Service
public class IciciDirectService {

    @Value("${icici.api.key}")
    private String apiKey;

    @Value("${icici.api.secret}")
    private String secretKey;

    @Value("${icici.api.base-url}")
    private String baseUrl;

    public ResponseEntity<String> getCustomerDetails(String sessionToken) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("SessionToken", sessionToken);
            requestBody.put("AppKey", apiKey);

            JSONObject response = HttpClientUtil.get(
                baseUrl + "/customerdetails",
                requestBody,
                apiKey, 
                sessionToken,
                secretKey
            );
            
            return ResponseEntity.ok(response.toString(2));
        } catch (IOException e) {
            throw new RuntimeException("Error fetching customer details: " + e.getMessage(), e);
        }
    }

    public ResponseEntity<String> placeOrder(JSONObject orderDetails) {
        try {
            // Extract sessionToken from orderDetails
            String sessionToken = orderDetails.optString("SessionToken");
            if (sessionToken == null || sessionToken.isEmpty())
                throw new IllegalArgumentException("SessionToken is required");

            JSONObject requestBody = new JSONObject();
            // Only add order fields, skip SessionToken and AppKey
            for (String key : orderDetails.keySet()) {
                if (key.equalsIgnoreCase("SessionToken") || key.equalsIgnoreCase("AppKey")) continue;
                Object value = orderDetails.get(key);
                if (value != null && !value.toString().isBlank()) {
                    requestBody.put(key, value);
                }
            }

            // Validate product if present (as per documentation)
            if (orderDetails.has("product")) {
                String product = orderDetails.optString("product");
                if (!product.isBlank()) {
                    var validProducts = java.util.List.of("futures", "options", "optionplus", "cash", "btst", "margin");
                    if (!validProducts.contains(product.toLowerCase())) {
                        throw new IllegalArgumentException("Invalid product type");
                    }
                }
            }

            JSONObject response = HttpClientUtil.post(
                baseUrl + "/order",
                requestBody,
                apiKey,
                sessionToken,
                secretKey
            );
            
            return ResponseEntity.ok(response.toString(2));
        } catch (IOException e) {
            throw new RuntimeException("Error placing order: " + e.getMessage(), e);
        }
    }

    /**
     * Get quotes for any instrument type (equity, futures, options)
     * Following Breeze-Java-SDK pattern
     * 
     * @param sessionToken Session token
     * @param exchangeCode Exchange code (NSE, BSE, NFO)
     * @param stockCode Stock code
     * @param productType Product type (cash, futures, options)
     * @param expiryDate Expiry date in ISO format (YYYY-MM-DD) - Required for F&O
     * @param right Option right (CE/PE) - Required for options
     * @param strikePrice Strike price - Required for options
     * @return Quotes response
     */
    public ResponseEntity<String> getQuotes(
            String sessionToken,
            String exchangeCode,
            String stockCode,
            String productType,
            String expiryDate,
            String right,
            String strikePrice) {
        try {
            // Validate required parameters
            if (sessionToken == null || sessionToken.isEmpty())
                throw new IllegalArgumentException("Session token is required");
            if (exchangeCode == null || exchangeCode.trim().isEmpty())
                throw new IllegalArgumentException("Exchange code is required");
            if (stockCode == null || stockCode.trim().isEmpty())
                throw new IllegalArgumentException("Stock code is required");

            // Validate productType if present
            if (productType != null && !productType.isBlank()) {
                List<String> validProductTypes = List.of("futures", "options", "optionplus", "cash", "btst", "margin");
                if (!validProductTypes.contains(productType.toLowerCase())) {
                    throw new IllegalArgumentException("Invalid product type");
                }
            }

            // Validate right if present
            if (right != null && !right.isBlank()) {
                List<String> validRights = List.of("call", "put", "others");
                if (!validRights.contains(right.toLowerCase())) {
                    throw new IllegalArgumentException("Invalid right type");
                }
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("session_token", sessionToken);
            requestBody.put("app_key", apiKey);
            requestBody.put("exchange_code", exchangeCode.trim().toUpperCase());
            requestBody.put("stock_code", stockCode.trim().toUpperCase());

            if (expiryDate != null && !expiryDate.isBlank()) {
                requestBody.put("expiry_date", expiryDate.trim());
            }
            if (productType != null && !productType.isBlank()) {
                requestBody.put("product_type", productType.trim().toLowerCase());
            }
            if (right != null && !right.isBlank()) {
                requestBody.put("right", right.trim().toLowerCase());
            }
            if (strikePrice != null && !strikePrice.isBlank()) {
                requestBody.put("strike_price", strikePrice.trim());
            }

            // Log the request for debugging
            System.out.println("Quotes Request: " + requestBody.toString(2));

            JSONObject response = HttpClientUtil.get(
                baseUrl + "/quotes",
                requestBody,
                apiKey,  
                sessionToken,             
                secretKey
            );
            
            // Log the response for debugging
            System.out.println("Quotes Response: " + response.toString(2));
            
            return ResponseEntity.ok(response.toString(2));
        } catch (IOException e) {
            System.err.println("Quotes Error: " + e.getMessage());
            throw new RuntimeException("Error fetching quotes: " + e.getMessage(), e);
        }
    }
} 