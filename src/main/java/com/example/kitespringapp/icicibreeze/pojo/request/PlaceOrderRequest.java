package com.example.kitespringapp.icicibreeze.pojo.request;

public class PlaceOrderRequest {
    private String sessionToken;
    private String stockCode;
    private String exchangeCode;
    private String product;
    private String action;
    private String quantity;
    private String expiryDate;
    private String right;
    private String strikePrice;
    private String price;
    private String orderType;
    private String validity;
    private String disclosedQuantity;
    // Add other fields as needed

    public PlaceOrderRequest() {}

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getExchangeCode() { return exchangeCode; }
    public void setExchangeCode(String exchangeCode) { this.exchangeCode = exchangeCode; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getRight() { return right; }
    public void setRight(String right) { this.right = right; }

    public String getStrikePrice() { return strikePrice; }
    public void setStrikePrice(String strikePrice) { this.strikePrice = strikePrice; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getValidity() { return validity; }
    public void setValidity(String validity) { this.validity = validity; }

    public String getDisclosedQuantity() { return disclosedQuantity; }
    public void setDisclosedQuantity(String disclosedQuantity) { this.disclosedQuantity = disclosedQuantity; }
} 