package com.example.demo.security;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

public class AiService {

    private final AiGatewayConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public AiService(AiGatewayConfig config) {
        this.config = config;
    }

    public String queryAi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getGatewayKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{ \"prompt\": \"" + prompt + "\" }";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                config.getGatewayUrl(),
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }
}