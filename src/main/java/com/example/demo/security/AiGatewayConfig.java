package com.example.demo.security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiGatewayConfig {

    @Value("${ai.gateway.url}")
    private String gatewayUrl;

    @Value("${ai.gateway.key}")
    private String gatewayKey;

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public String getGatewayKey() {
        return gatewayKey;
    }
}