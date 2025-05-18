package com.example.kitespringapp.controller;

import com.example.kitespringapp.entity.PnLRecord;
import com.example.kitespringapp.repository.PnLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pnl-data")
public class PnLDataController {

    private final PnLRepository pnlRepository;

    @Autowired
    public PnLDataController(PnLRepository pnlRepository) {
        this.pnlRepository = pnlRepository;
    }

    @GetMapping("/recent")
    public List<PnLRecord> getRecentData(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startTime) {
        
        if (startTime == null) {
            startTime = LocalDateTime.now().minusHours(1); // Default to last hour
        }
        
        List<PnLRecord> records = pnlRepository.findRecentRecords(startTime);
        System.out.println("Fetching PnL records since: " + startTime + ", found: " + records.size() + " records");
        return records;
    }

    @GetMapping("/test")
    public String testData() {
        LocalDateTime now = LocalDateTime.now();
        PnLRecord testRecord = new PnLRecord(now, 1000.0, 500.0, 500.0);
        pnlRepository.save(testRecord);
        return "Test record added with timestamp: " + now;
    }
} 