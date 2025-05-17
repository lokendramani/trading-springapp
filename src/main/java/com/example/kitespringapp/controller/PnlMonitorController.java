package com.example.kitespringapp.controller;

import com.example.kitespringapp.service.PnLMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/monitor")
public class PnlMonitorController {

    @Autowired
    private PnLMonitorService pnlMonitorService;

    @GetMapping("/start")
    public ResponseEntity<String> startMonitoring() {
        pnlMonitorService.startMonitoring();
        return ResponseEntity.ok("P&L Monitoring started.");
    }
}
