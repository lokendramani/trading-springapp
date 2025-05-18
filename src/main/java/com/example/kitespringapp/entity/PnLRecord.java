package com.example.kitespringapp.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pnl_records")
public class PnLRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private double pnlValue;

    @Column(nullable = false)
    private double ceValue;

    @Column(nullable = false)
    private double peValue;

    // Default constructor
    public PnLRecord() {
    }

    // Constructor with fields
    public PnLRecord(LocalDateTime timestamp, double pnlValue, double ceValue, double peValue) {
        this.timestamp = timestamp;
        this.pnlValue = pnlValue;
        this.ceValue = ceValue;
        this.peValue = peValue;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getPnlValue() {
        return pnlValue;
    }

    public void setPnlValue(double pnlValue) {
        this.pnlValue = pnlValue;
    }

    public double getCeValue() {
        return ceValue;
    }

    public void setCeValue(double ceValue) {
        this.ceValue = ceValue;
    }

    public double getPeValue() {
        return peValue;
    }

    public void setPeValue(double peValue) {
        this.peValue = peValue;
    }
} 