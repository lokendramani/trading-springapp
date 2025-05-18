package com.example.kitespringapp.pojo;

public class StrategyResponse {
    private double niftyOpenPrice;
    private int strikePrice;
    private String ceSymbol;
    private String peSymbol;
    private String status;
    private String message;

    public double getNiftyOpenPrice() {
        return niftyOpenPrice;
    }

    public void setNiftyOpenPrice(double niftyOpenPrice) {
        this.niftyOpenPrice = niftyOpenPrice;
    }

    public int getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(int strikePrice) {
        this.strikePrice = strikePrice;
    }

    public String getCeSymbol() {
        return ceSymbol;
    }

    public void setCeSymbol(String ceSymbol) {
        this.ceSymbol = ceSymbol;
    }

    public String getPeSymbol() {
        return peSymbol;
    }

    public void setPeSymbol(String peSymbol) {
        this.peSymbol = peSymbol;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 