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

    private static class PositionDetails {
        private final double totalPnL;
        private final double cePnL;
        private final double pePnL;
        private final double ceAvgPrice;
        private final double ceLtp;
        private final int ceQuantity;
        private final double peAvgPrice;
        private final double peLtp;
        private final int peQuantity;

        public PositionDetails(double totalPnL, double cePnL, double pePnL,
                             double ceAvgPrice, double ceLtp, int ceQuantity,
                             double peAvgPrice, double peLtp, int peQuantity) {
            this.totalPnL = totalPnL;
            this.cePnL = cePnL;
            this.pePnL = pePnL;
            this.ceAvgPrice = ceAvgPrice;
            this.ceLtp = ceLtp;
            this.ceQuantity = ceQuantity;
            this.peAvgPrice = peAvgPrice;
            this.peLtp = peLtp;
            this.peQuantity = peQuantity;
        }

        public double getTotalPnL() { return totalPnL; }
        public double getCePnL() { return cePnL; }
        public double getPePnL() { return pePnL; }
        public double getCeAvgPrice() { return ceAvgPrice; }
        public double getCeLtp() { return ceLtp; }
        public int getCeQuantity() { return ceQuantity; }
        public double getPeAvgPrice() { return peAvgPrice; }
        public double getPeLtp() { return peLtp; }
        public int getPeQuantity() { return peQuantity; }
    }

    public PositionDetails getCurrentPnL(PositionsResponse positionsResponse) {
        if (positionsResponse == null || positionsResponse.getData() == null) {
            return new PositionDetails(0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0, 0);
        }

        List<NetItem> netItems = positionsResponse.getData().getNet().stream()
                .filter(pos -> { 
                    String symbol = pos.getTradingsymbol();
                    return symbol != null && (symbol.endsWith("CE") || symbol.endsWith("PE")) && pos.getQuantity() != 0;
                }).collect(Collectors.toList());

        Map<String, Double> symbolToLtp = fetchLtpForSymbols(netItems);

        double totalPnL = 0.0;
        double cePnL = 0.0;
        double pePnL = 0.0;
        double ceAvgPrice = 0.0;
        double ceLtp = 0.0;
        int ceQuantity = 0;
        double peAvgPrice = 0.0;
        double peLtp = 0.0;
        int peQuantity = 0;

        for (NetItem item : netItems) {
            String key = item.getExchange() + ":" + item.getTradingsymbol();
            Double ltp = symbolToLtp.get(key);
            if (ltp != null) {
                int qty = item.getQuantity();
                double avgPrice = item.getAveragePrice();
                double positionPnL = qty > 0 ? (ltp - avgPrice) * qty : (avgPrice - ltp) * Math.abs(qty);
                totalPnL += positionPnL;

                if (item.getTradingsymbol().endsWith("CE")) {
                    cePnL = positionPnL;
                    ceAvgPrice = avgPrice;
                    ceLtp = ltp;
                    ceQuantity = qty;
                } else if (item.getTradingsymbol().endsWith("PE")) {
                    pePnL = positionPnL;
                    peAvgPrice = avgPrice;
                    peLtp = ltp;
                    peQuantity = qty;
                }
                
                System.out.println(item.getTradingsymbol() + " LTP:" + ltp + " and pnl " + positionPnL);
            }
        }

        // Save PnL record
        savePnLRecord(totalPnL, cePnL, pePnL);

        return new PositionDetails(totalPnL, cePnL, pePnL, 
                                 ceAvgPrice, ceLtp, ceQuantity,
                                 peAvgPrice, peLtp, peQuantity);
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

    private void printPositionDetails(PositionsResponse positionsResponse, LocalTime currentTime, PositionDetails details) {
        System.out.println("\n=== Position Update at " + currentTime + " ===");
        System.out.println("Current PnL: " + details.getTotalPnL());
        
        System.out.println("\nPosition Details:");
        if (details.getCeQuantity() != 0) {
            System.out.printf("CE Position - Avg Price: %.2f, LTP: %.2f, Quantity: %d, PnL: %.2f%n", 
                details.getCeAvgPrice(), details.getCeLtp(), details.getCeQuantity(), details.getCePnL());
        }
        if (details.getPeQuantity() != 0) {
            System.out.printf("PE Position - Avg Price: %.2f, LTP: %.2f, Quantity: %d, PnL: %.2f%n", 
                details.getPeAvgPrice(), details.getPeLtp(), details.getPeQuantity(), details.getPePnL());
        }
        
        if (details.getCeQuantity() != 0 || details.getPeQuantity() != 0) {
            System.out.printf("Total PnL: %.2f (CE: %.2f, PE: %.2f)%n", 
                details.getTotalPnL(), details.getCePnL(), details.getPePnL());
        }
        System.out.println("================================\n");
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

        // Trailing stop-loss configuration
        final double trailingStartThreshold = 2000.0; // Start trailing after this profit
        final double trailingDropAmount = 1000.0;     // Trail if PnL drops by this amount
        final double[] peakPnL = {Double.NEGATIVE_INFINITY}; // Use array to mutate inside lambda

        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalTime currentTime = LocalTime.now();
                PositionsResponse positionsResponse = fetchPositions();
                
                if (positionsResponse == null) {
                    System.err.println("Failed to fetch positions after retries");
                    return;
                }
                
                PositionDetails details = getCurrentPnL(positionsResponse);
                printPositionDetails(positionsResponse, currentTime, details);

                // Update peakPnL if currentPnL is a new high
                if (details.getTotalPnL() > peakPnL[0]) {
                    peakPnL[0] = details.getTotalPnL();
                }
                // Check trailing stop-loss condition only after threshold is crossed
                if (peakPnL[0] >= trailingStartThreshold &&
                        (peakPnL[0] - details.getTotalPnL()) >= trailingDropAmount) {
                    System.out.println("Trailing Stop Loss Triggered. Peak PnL: " + peakPnL[0] + ", Current PnL: " + details.getTotalPnL());
                    if (squareOffAll(positionsResponse)) {
                        stopMonitoring();
                    }
                }

                if (details.getTotalPnL() < stopLossThreshold) {
                    System.out.println("Stop Loss Triggered at PnL: " + details.getTotalPnL());
                    if (squareOffAll(positionsResponse)) {
                        stopMonitoring();
                    }
                } else if (currentTime.isAfter(LocalTime.of(19, 01))) {
                    System.out.println("EOD Exit at PnL: " + details.getTotalPnL());
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
