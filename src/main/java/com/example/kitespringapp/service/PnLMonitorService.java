package com.example.kitespringapp.service;

import com.example.kitespringapp.pojo.NetItem;
import com.example.kitespringapp.pojo.PositionsResponse;
import com.example.kitespringapp.entity.PnLRecord;
import com.example.kitespringapp.repository.PnLRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PnLMonitorService {

    private final RestTemplate restTemplate;
    @Value("${kite.api.base-url}")
    private String baseUrl;
    private static final String POSITIONS_URL = "/portfolio/positions";
    private static final String LTP_URL = "/quote/ltp";
    private static final String ORDER_URL = "/orders/regular";

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;
    private double stopLossThreshold = -2000.0;  // Changed to non-final for updateStopLoss
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    private String accessToken;
    private String apiKey;

    @Autowired
    private PnLRepository pnlRepository;

    public PnLMonitorService() {
        this.restTemplate = new RestTemplate();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private PositionsResponse fetchPositions() {
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = baseUrl + POSITIONS_URL;
        
        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                System.out.println("Fetching positions from " + url);
                ResponseEntity<PositionsResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, PositionsResponse.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response.getBody();
                }
            } catch (Exception e) {
                System.err.println("Attempt " + (retry + 1) + " failed: " + e.getMessage());
                if (retry < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return null;
    }

    public double getCurrentPnL(PositionsResponse positionsResponse) {
        if (positionsResponse == null || positionsResponse.getData() == null) return 0.0;

        List<NetItem> netItems = positionsResponse.getData().getNet().stream()
                .filter(pos -> {
                    String symbol = pos.getTradingsymbol();
                    return symbol != null && (symbol.endsWith("CE") || symbol.endsWith("PE")) && pos.getQuantity() != 0;
                }).collect(Collectors.toList());

        Map<String, Double> symbolToLtp = fetchLtpForSymbols(netItems);

        double grossPnl = 0.0;
        double ceValue = 0.0;
        double peValue = 0.0;

        for (NetItem item : netItems) {
            String key = item.getExchange() + ":" + item.getTradingsymbol();
            Double ltp = symbolToLtp.get(key);
            if (ltp != null) {
                int qty = item.getQuantity();
                double avgPrice = item.getAveragePrice();
                double pnl = qty > 0 ? (ltp - avgPrice) * qty : (avgPrice - ltp) * Math.abs(qty);
                grossPnl += pnl;
                
                // Calculate CE and PE values using LTP
                if (item.getTradingsymbol().endsWith("CE")) {
                    ceValue = ltp * Math.abs(qty);
                } else if (item.getTradingsymbol().endsWith("PE")) {
                    peValue = ltp * Math.abs(qty);
                }
                
                System.out.println(item.getTradingsymbol()+" LTP:" + ltp + " and pnl " + pnl);
            }
        }

        // Save PnL record with LTP-based values
        savePnLRecord(grossPnl, ceValue, peValue);

        return grossPnl;
    }


    private Map<String, Double> fetchLtpForSymbols(List<NetItem> netItems) {
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        String instrumentsQuery = netItems.stream()
                .map(item -> item.getExchange() + ":" + item.getTradingsymbol())
                .distinct()
                .collect(Collectors.joining("&i=", "?i=", ""));

        String url = baseUrl + LTP_URL + instrumentsQuery;

        try {
            System.out.println("Fetching LTP from " + url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String body = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(body);
                JsonNode data = root.path("data");

                Map<String, Double> ltpMap = new HashMap<>();
                // Iterate over all fields in the data object
                Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    String instrumentKey = field.getKey();
                    double lastPrice = field.getValue().path("last_price").asDouble();
                    ltpMap.put(instrumentKey, lastPrice);
                }
                return ltpMap;
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch LTPs: " + e.getMessage());
        }
        return Collections.emptyMap();
    }


    public boolean squareOffAll(PositionsResponse positionsResponse) {
        try {
            if (positionsResponse == null || positionsResponse.getData() == null) return false;

            for (NetItem position : positionsResponse.getData().getNet()) {
                int quantity = position.getQuantity();
                if (quantity == 0) continue;

                String tradingsymbol = position.getTradingsymbol();
                String exchange = position.getExchange();
                String product = position.getProduct();
                String transactionType = quantity > 0 ? "SELL" : "BUY";
                int absQty = Math.abs(quantity);

                HttpHeaders orderHeaders = getAuthHeaders();
                orderHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                String body = "exchange=" + exchange +
                        "&tradingsymbol=" + tradingsymbol +
                        "&transaction_type=" + transactionType +
                        "&order_type=MARKET" +
                        "&quantity=" + absQty +
                        "&product=" + product +
                        "&validity=DAY";

                HttpEntity<String> orderEntity = new HttpEntity<>(body, orderHeaders);
                String url = baseUrl + ORDER_URL;
                System.out.println("Square off url " + url);
                ResponseEntity<String> orderResponse = restTemplate.postForEntity(url, orderEntity, String.class);

                if (!orderResponse.getStatusCode().is2xxSuccessful()) {
                    System.err.println("Failed to square off: " + tradingsymbol);
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Exception during squareOffAll: " + e.getMessage());
            return false;
        }
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Kite-Version", "3");
        headers.set("Authorization", "token " + apiKey + ":" + accessToken);
        return headers;
    }

    public void updateStopLoss(double newStopLoss) {
        if (newStopLoss >= 0) {
            throw new IllegalArgumentException("Stop loss must be a negative value");
        }
        this.stopLossThreshold = newStopLoss;
        System.out.println("Stop loss threshold updated to: " + stopLossThreshold);
    }

    private void printPositionDetails(PositionsResponse positionsResponse, LocalTime currentTime, double currentPnL) {
        System.out.println("\n=== Position Update at " + currentTime + " ===");
        System.out.println("Current PnL: " + currentPnL);
        
        if (positionsResponse.getData() != null && positionsResponse.getData().getNet() != null) {
            List<NetItem> positions = positionsResponse.getData().getNet();
            
            // Track CE and PE positions
            double totalCEValue = 0;
            int totalCEQty = 0;
            double cePriceForStraddle = 0;
            
            double totalPEValue = 0;
            int totalPEQty = 0;
            double pePriceForStraddle = 0;

            for (NetItem pos : positions) {
                String symbol = pos.getTradingsymbol();
                int quantity = pos.getQuantity();
                double avgPrice = pos.getAveragePrice();
                
                // Only process short positions (negative quantity)
                if (symbol != null && quantity < 0) {
                    if (symbol.endsWith("CE")) {
                        totalCEValue = Math.abs(avgPrice * quantity);  // Total value of CE positions
                        totalCEQty = quantity;
                        cePriceForStraddle = avgPrice;
                    } else if (symbol.endsWith("PE")) {
                        totalPEValue = Math.abs(avgPrice * quantity);  // Total value of PE positions
                        totalPEQty = quantity;
                        pePriceForStraddle = avgPrice;
                    }
                }
            }

            System.out.println("\nStraddle Details:");
            if (totalCEQty < 0) {  // Check for negative quantity (short position)
                System.out.printf("CE Short Price: %.2f (Qty: %d, Total Value: %.2f)%n", 
                    cePriceForStraddle, totalCEQty, totalCEValue);
            }
            if (totalPEQty < 0) {  // Check for negative quantity (short position)
                System.out.printf("PE Short Price: %.2f (Qty: %d, Total Value: %.2f)%n", 
                    pePriceForStraddle, totalPEQty, totalPEValue);
            }
            
            if (totalCEQty < 0 && totalPEQty < 0) {  // Only show total if both legs are short
                double totalStraddleValue = totalCEValue + totalPEValue;  // Sum of both legs' total values
                System.out.printf("Total Straddle Value: %.2f (Total Qty: %d)%n", 
                    totalStraddleValue, totalCEQty + totalPEQty);
            }
            System.out.println("================================\n");
        }
    }

    private void savePnLRecord(double pnlValue, double ceValue, double peValue) {
        PnLRecord record = new PnLRecord(
            LocalDateTime.now(),
            pnlValue,
            ceValue,
            peValue
        );
        PnLRecord savedRecord = pnlRepository.save(record);
        System.out.println("Saved PnL Record - Time: " + savedRecord.getTimestamp() + 
                          ", PnL: " + savedRecord.getPnlValue() + 
                          ", CE: " + savedRecord.getCeValue() + 
                          ", PE: " + savedRecord.getPeValue());
    }

    public void startMonitoring(String apiKey, String accessToken) {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            System.out.println("Monitoring already started");
            return;
        }
        this.apiKey = apiKey;
        this.accessToken = accessToken;

        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalTime currentTime = LocalTime.now();
                PositionsResponse positionsResponse = fetchPositions();
                
                if (positionsResponse == null) {
                    System.err.println("Failed to fetch positions after retries");
                    return;
                }
                
                double currentPnL = getCurrentPnL(positionsResponse);
                
                printPositionDetails(positionsResponse, currentTime, currentPnL);

                if (currentPnL < stopLossThreshold) {
                    System.out.println("Stop Loss Triggered at PnL: " + currentPnL);
                    if (squareOffAll(positionsResponse)) {
                        stopMonitoring();
                    }
                } else if (currentTime.isAfter(LocalTime.of(15, 01))) {
                    System.out.println("EOD Exit at PnL: " + currentPnL);
                    if (squareOffAll(positionsResponse)) {
                        stopMonitoring();
                    }
                }
            } catch (Exception e) {
                System.err.println("Monitoring error: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stopMonitoring() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(true);
            System.out.println("Monitoring stopped");
        }
    }
}
