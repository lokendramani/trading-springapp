package com.example.kitespringapp.controller;

import com.example.kitespringapp.service.PnLMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/monitor")
public class PnlMonitorController {

    @Value("${kite.api.key}")
    private String api_key;
    
    @Value("${kite.accessToken}")
    private String accessToken;

    @Value("${kite.api.base-url}")
    private String baseUrl;

    @Autowired
    private PnLMonitorService pnlMonitorService;

    @GetMapping("/start")
    public ResponseEntity<String> startMonitoring() {
        try {
            pnlMonitorService.startMonitoring(api_key, accessToken);
            return ResponseEntity.ok("P&L Monitoring started successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to start monitoring: " + e.getMessage());
        }
    }

    @GetMapping("/stop")
    public ResponseEntity<String> stopMonitoring() {
        try {
            pnlMonitorService.stopMonitoring();
            return ResponseEntity.ok("P&L Monitoring stopped successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to stop monitoring: " + e.getMessage());
        }
    }

    @PostMapping("/update-stoploss")
    public ResponseEntity<String> updateStopLoss(@RequestParam double newStopLoss) {
        try {
            pnlMonitorService.updateStopLoss(newStopLoss);
            return ResponseEntity.ok("Stop loss updated to: " + newStopLoss);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to update stop loss: " + e.getMessage());
        }
    }

    @PostConstruct  
    public void printBaseUrl() {
        System.out.println("Kite Base URL: " + baseUrl);
    }
}
