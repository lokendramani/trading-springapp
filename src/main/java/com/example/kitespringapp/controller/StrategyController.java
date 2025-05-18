package com.example.kitespringapp.controller;

import com.example.kitespringapp.service.StrategyService;
import com.example.kitespringapp.pojo.StrategyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {

    private final StrategyService strategyService;

    @Autowired
    public StrategyController(StrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @PostMapping("/short-straddle")
    public ResponseEntity<StrategyResponse> executeShortStraddle() {
        try {
            StrategyResponse response = strategyService.executeNiftyStraddle();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            StrategyResponse errorResponse = new StrategyResponse();
            errorResponse.setStatus("ERROR");
            errorResponse.setMessage("Strategy execution failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 