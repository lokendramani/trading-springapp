package com.example.kitespringapp.icicibreeze.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.kitespringapp.icicibreeze.service.IciciDirectService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.example.kitespringapp.icicibreeze.pojo.request.PlaceOrderRequest;
import org.json.JSONObject;

@RestController
@RequestMapping("/api/icici")
public class IciciDirectController {

    private final IciciDirectService iciciDirectService;

    @Autowired
    public IciciDirectController(IciciDirectService iciciDirectService) {
        this.iciciDirectService = iciciDirectService;
    }

    @GetMapping("/customer-details")
    public ResponseEntity<String> getCustomerDetails(@RequestParam String sessionToken) {
        try {
            return iciciDirectService.getCustomerDetails(sessionToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching customer details: " + e.getMessage());
        }
    }

    @GetMapping("/quotes")
    public ResponseEntity<String> getQuotes(
            @RequestParam String sessionToken,
            @RequestParam String exchangeCode,
            @RequestParam String stockCode,
            @RequestParam String productType,
            @RequestParam String expiryDate,
            @RequestParam String right,
            @RequestParam String strikePrice) {
        try {
            return iciciDirectService.getQuotes(
                sessionToken, exchangeCode, stockCode, productType, 
                expiryDate, right, strikePrice);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching quotes: " + e.getMessage());
        }
    }

    @PostMapping("/order")
    public ResponseEntity<String> placeOrder(@RequestBody PlaceOrderRequest req) {
        try {
            JSONObject orderDetails = new JSONObject();
            if (req.getSessionToken() != null) orderDetails.put("SessionToken", req.getSessionToken());
            if (req.getStockCode() != null) orderDetails.put("stock_code", req.getStockCode());
            if (req.getExchangeCode() != null) orderDetails.put("exchange_code", req.getExchangeCode());
            if (req.getProduct() != null) orderDetails.put("product", req.getProduct());
            if (req.getAction() != null) orderDetails.put("action", req.getAction());
            if (req.getQuantity() != null) orderDetails.put("quantity", req.getQuantity());
            if (req.getExpiryDate() != null) orderDetails.put("expiry_date", req.getExpiryDate());
            if (req.getRight() != null) orderDetails.put("right", req.getRight());
            if (req.getStrikePrice() != null) orderDetails.put("strike_price", req.getStrikePrice());
            if (req.getPrice() != null) orderDetails.put("price", req.getPrice());
            if (req.getOrderType() != null) orderDetails.put("order_type", req.getOrderType());
            if (req.getValidity() != null) orderDetails.put("validity", req.getValidity());
            if (req.getDisclosedQuantity() != null) orderDetails.put("disclosed_quantity", req.getDisclosedQuantity());
            // Add other fields as needed
            return iciciDirectService.placeOrder(orderDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error placing order: " + e.getMessage());
        }
    }
} 