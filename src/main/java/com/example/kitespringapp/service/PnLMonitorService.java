package com.example.kitespringapp.service;

import com.example.kitespringapp.pojo.NetItem;
import com.example.kitespringapp.pojo.PositionsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PnLMonitorService {

    private final RestTemplate restTemplate;

    private static final String ACCESS_TOKEN = "";
    private static final String API_KEY = "";
    private static final String POSITIONS_URL = "https://api.kite.trade/portfolio/positions";
    private static final String LTP_URL = "https://api.kite.trade/quote/ltp";
    private static final String ORDER_URL = "https://api.kite.trade/orders/regular";

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;
    private final double MAX_LOSS_THRESHOLD = -2000.0;

    public PnLMonitorService() {
        this.restTemplate = new RestTemplate();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private PositionsResponse fetchPositions() {
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<PositionsResponse> response = restTemplate.exchange(
                    POSITIONS_URL, HttpMethod.GET, entity, PositionsResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.out.println("Exception occurred in fetching positions: " + e.getMessage());
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
        for (NetItem item : netItems) {
            String key = item.getExchange() + ":" + item.getTradingsymbol();
            Double ltp = symbolToLtp.get(key);
            if (ltp != null) {
                int qty = item.getQuantity();
                double avgPrice = item.getAveragePrice();
                double pnl = qty > 0 ? (ltp - avgPrice) * qty : (avgPrice - ltp) * Math.abs(qty);
                grossPnl += pnl;
            }
        }

        return grossPnl;
    }

    private Map<String, Double> fetchLtpForSymbols(List<NetItem> netItems) {
        HttpHeaders headers = getAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String instrumentsQuery = netItems.stream()
                .map(item -> item.getExchange() + ":" + item.getTradingsymbol())
                .distinct()
                .collect(Collectors.joining("&i=", "?i=", ""));

        String url = LTP_URL + instrumentsQuery;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String body = response.getBody();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(body);

                Map<String, Double> ltpMap = new HashMap<>();
                for (String key : netItems.stream().map(i -> i.getExchange() + ":" + i.getTradingsymbol()).distinct().toList()) {
                    JsonNode quoteNode = root.path("data").path(key);
                    double lastPrice = quoteNode.path("last_price").asDouble();
                    ltpMap.put(key, lastPrice);
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
                ResponseEntity<String> orderResponse = restTemplate.postForEntity(ORDER_URL, orderEntity, String.class);

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
        headers.set("Authorization", "token " + API_KEY + ":" + ACCESS_TOKEN);
        return headers;
    }

    public void startMonitoring() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            System.out.println("Monitoring already started");
            return;
        }

        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                LocalTime currentTime = LocalTime.now();
                PositionsResponse positionsResponse = fetchPositions();
                double currentPnL = getCurrentPnL(positionsResponse);
                System.out.println("Current PnL: " + currentPnL + " at " + currentTime);

                if (currentPnL < MAX_LOSS_THRESHOLD) {
                    System.out.println("Stop Loss Triggered");
                    if (squareOffAll(positionsResponse)) stopMonitoring();
                } else if (currentTime.isAfter(LocalTime.of(15, 1))) {
                    System.out.println("EOD Exit");
                    if (squareOffAll(positionsResponse)) stopMonitoring();
                }
            } catch (Exception e) {
                System.err.println("Monitoring error: " + e.getMessage());
            }
        }, 0, 6, TimeUnit.MINUTES);
    }

    private void stopMonitoring() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(true);
            System.out.println("Monitoring stopped");
        }
    }
}
