package com.carparking.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MpesaConfig {

    // ── Sandbox credentials — swap for production later ──
    // Get yours at: https://developer.safaricom.co.ke
    @Value("${mpesa.consumer.key:your_consumer_key_here}")
    public String consumerKey;

    @Value("${mpesa.consumer.secret:your_consumer_secret_here}")
    public String consumerSecret;

    @Value("${mpesa.shortcode:174379}")
    public String shortcode;           // Sandbox shortcode

    @Value("${mpesa.passkey}")
    public String passkey;

    @Value("${mpesa.callback.url:https://yourdomain.com/api/mpesa/callback}")
    public String callbackUrl;

    @Value("${mpesa.base.url:https://sandbox.safaricom.co.ke}")
    public String baseUrl;             // Change to https://api.safaricom.co.ke for production

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}