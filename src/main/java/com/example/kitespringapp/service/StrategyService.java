package com.example.kitespringapp.service;

import com.example.kitespringapp.entity.StrategyPosition;
import com.example.kitespringapp.repository.StrategyPositionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.kitespringapp.pojo.StrategyResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class StrategyService {

    private final RestTemplate restTemplate;
    
    @Value("${kite.api.base-url}")
    private String baseUrl;
    
    @Value("${kite.api.key}")
    private String apiKey;
    
    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private StrategyPositionsRepository repo;

    private static final String QUOTE_URL = "/quote/ohlc";
    private static final String ORDER_URL = "/orders/regular";
    private static final String NIFTY_INDEX = "NSE:NIFTY 50";

    public StrategyService() {
        this.restTemplate = new RestTemplate();
    }

    public StrategyResponse executeNiftyStraddle() {
        StrategyResponse response = new StrategyResponse();
        try {
            // 1. Get Nifty opening price
            double niftyPrice = getNiftyOpenPrice();
            System.out.println("Nifty opening price: " + niftyPrice);
            response.setNiftyOpenPrice(niftyPrice);

            // 2. Round to nearest 50
            int strikePrice = roundToNearestStrike(niftyPrice);
            System.out.println("Selected strike price: " + strikePrice);
            response.setStrikePrice(strikePrice);

            // 3. Generate option symbols with monthly expiry
            String expiryDate = getMonthlyExpiryDate();
            String ceSymbol = String.format("NIFTY%s%d%s", expiryDate, strikePrice, "CE");
            String peSymbol = String.format("NIFTY%s%d%s", expiryDate, strikePrice, "PE");

            System.out.println("CE Symbol: " + ceSymbol);
            System.out.println("PE Symbol: " + peSymbol);
            
            response.setCeSymbol(ceSymbol);
            response.setPeSymbol(peSymbol);

            // 4. Place CE order
            placeOrder(ceSymbol, "SELL", 75);

            // 5. Place PE order
            placeOrder(peSymbol, "SELL", 75);

            createPostions("NiftyStraddle",ceSymbol,peSymbol);
            response.setStatus("SUCCESS");
            response.setMessage("Short straddle orders placed successfully");
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Strategy execution failed: " + e.getMessage(), e);
        }
    }

    private double getNiftyOpenPrice() {
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        String url = baseUrl + QUOTE_URL + "?i=" + NIFTY_INDEX;
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, String.class);
            
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                return root.path("data")
                          .path(NIFTY_INDEX)
                          .path("ohlc")
                          .path("open")
                          .asDouble();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Nifty opening price", e);
            }
        }
        throw new RuntimeException("Failed to fetch Nifty price");
    }

    private int roundToNearestStrike(double price) {
        return (int) (Math.round(price / 50.0) * 50);
    }

    private void placeOrder(String symbol, String transactionType, int quantity) {
        HttpHeaders headers = getAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = String.format(
            "exchange=NFO&tradingsymbol=%s&transaction_type=%s&order_type=MARKET&quantity=%d&product=NRML&validity=DAY",
            symbol, transactionType, quantity
        );

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        String url = baseUrl + ORDER_URL;

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to place order for " + symbol);
        }
        
        System.out.println("Order placed successfully for " + symbol);
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Kite-Version", "3");
        headers.set("Authorization", "token " + apiKey + ":" + accessTokenService.getAccessToken());
        return headers;
    }

    private String getMonthlyExpiryDate() {
        LocalDate today = LocalDate.now();
        String strikePrice = String.format("%02d%s", today.getYear() % 100, today.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase());
        // Format: 25MAY (for May 2024)
        System.out.println("Strike Price: " + strikePrice);
        return strikePrice;
    }

    private void createPostions(String strategyName, String ceSymbol, String peSymbol) {
        repo.save(new StrategyPosition(ceSymbol, strategyName, "CE"));
        repo.save(new StrategyPosition(peSymbol, strategyName, "PE"));
    }
} 