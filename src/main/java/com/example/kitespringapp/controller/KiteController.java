package com.example.kitespringapp.controller;

import com.example.kitespringapp.pojo.accessResponse.AccessTokenResponse;
import com.example.kitespringapp.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@RestController
public class KiteController {

    @Value("${kite.api.key}")
    private String apiKey;

    @Value("${kite.api.secret}")
    private String apiSecret;

    @Autowired
    private AccessTokenService  accessTokenService;

    @GetMapping("/kite/login")
    public String getLoginUrl() {
        return "https://kite.trade/connect/login?api_key=" + apiKey;
    }

    @PostMapping("/kite/access-token")
    public ResponseEntity<AccessTokenResponse> generateAccessToken(@RequestParam String requestToken) {
        String url = "https://api.kite.trade/session/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> params = new HashMap<>();
        params.put("api_key", apiKey);
        params.put("request_token", requestToken);
        params.put("checksum", org.apache.commons.codec.digest.DigestUtils.sha256Hex(apiKey + requestToken + apiSecret));

        String body = "api_key=" + apiKey + "&request_token=" + requestToken + "&checksum=" + params.get("checksum");

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, AccessTokenResponse.class);
        accessTokenService.setAccessToken(response.getBody().getData().getAccessToken());
        System.out.println("AccessToken:"+response.getBody().getData().getAccessToken());

        return response;
    }
}
