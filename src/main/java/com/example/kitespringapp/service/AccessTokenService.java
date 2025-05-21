package com.example.kitespringapp.service;

import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {
    private String accessToken;

    public synchronized void setAccessToken(String token) {
        this.accessToken = token;
    }

    public synchronized String getAccessToken() {
        return accessToken;
    }
}

