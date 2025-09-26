package com.example.chequeprocessing.sayad;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class SayadClient {
    private final RestTemplate restTemplate;
    private final SayadProperties properties;

    public SayadClient(RestTemplate restTemplate, SayadProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public boolean registerCheque(String number) {
        String url = properties.getBaseUrl() + "/register";
        ResponseEntity<String> response = restTemplate.postForEntity(url, Map.of("number", number), String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean presentCheque(String number) {
        String url = properties.getBaseUrl() + "/present";
        ResponseEntity<String> response = restTemplate.postForEntity(url, Map.of("number", number), String.class);
        return response.getStatusCode().is2xxSuccessful();
    }
}


