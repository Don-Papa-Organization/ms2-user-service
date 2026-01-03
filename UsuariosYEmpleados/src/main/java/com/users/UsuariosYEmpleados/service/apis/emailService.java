package com.users.UsuariosYEmpleados.service.apis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {
    
    private final RestTemplate restTemplate;
    private final String emailServiceUrl;
    
    public EmailService(
        @Value("${EMAIL_SERVICE_URL:http://email-service:4007}") String emailServiceUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.emailServiceUrl = emailServiceUrl;
    }
    
    public Object sendVerificationEmail(String email, String token) {
        try {
            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Request body (exactamente como en el código original)
            String requestBody = String.format(
                "{\"email\":\"%s\",\"token\":\"%s\"}", 
                email, 
                token
            );
            
            // HttpEntity
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            // URL exactamente como en el código original
            String url = emailServiceUrl + "/sendMail/verification";
            
            // POST request
            ResponseEntity<String> response = restTemplate.postForEntity(
                url, 
                request, 
                String.class
            );
            
            return response.getBody();
            
        } catch (Exception error) {
            // Log exactamente como en el código original
            System.out.println("[sendVerificationEmail] error: " + 
                (error.getMessage() != null ? error.getMessage() : error));
            throw new RuntimeException(error);
        }
    }
}