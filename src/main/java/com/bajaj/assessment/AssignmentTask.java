package com.bajaj.assessment;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class AssignmentTask implements CommandLineRunner {


    private static final String MY_NAME = "OZA ASHWIN KUMAR RAMANBHAI";
    private static final String MY_REG_NO = "22BCE7932";
    private static final String MY_EMAIL = "ashwinkumar.22bce7932@vitapstudent.ac.in";

    // API ENDPOINTS
    private static final String GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    @Override
    public void run(String... args) {
        System.out.println(">>> APP STARTED. RUNNING ASSIGNMENT LOGIC...");

        RestTemplate restTemplate = new RestTemplate();

        // --- STEP 1: Generate Webhook ---
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", MY_NAME);
        requestBody.put("regNo", MY_REG_NO);
        requestBody.put("email", MY_EMAIL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            System.out.println(">>> Sending request to generate webhook...");
            ResponseEntity<Map> response = restTemplate.postForEntity(GENERATE_URL, requestEntity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null) {
                String accessToken = (String) responseBody.get("accessToken");
                String webhookUrl = (String) responseBody.get("webhookUrl");

                // Fallback URL if the API returns null (safety check)
                if (webhookUrl == null) {
                    webhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
                }

                System.out.println(">>> Received Token: " + accessToken);
                System.out.println(">>> Target Webhook URL: " + webhookUrl);

                // --- STEP 2: Solve the SQL Question ---
                // Based on the 'SQL Qwestion 2 JAVA.pdf' you uploaded
                String finalSqlQuery = getSolutionForQuestion2();

                System.out.println(">>> Generated SQL Query: " + finalSqlQuery);

                // --- STEP 3: Submit Solution ---
                HttpHeaders authHeaders = new HttpHeaders();
                authHeaders.setContentType(MediaType.APPLICATION_JSON);
                authHeaders.set("Authorization", accessToken); // JWT Token requirement

                Map<String, String> submitBody = new HashMap<>();
                submitBody.put("finalQuery", finalSqlQuery);

                HttpEntity<Map<String, String>> submitEntity = new HttpEntity<>(submitBody, authHeaders);

                System.out.println(">>> Submitting solution...");
                ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhookUrl, submitEntity, String.class);

                System.out.println(">>> SERVER RESPONSE: " + submitResponse.getBody());
            }
        } catch (Exception e) {
            System.err.println(">>> ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Logic for Question 2 (Even RegNo)
    private String getSolutionForQuestion2() {
        // REQUIREMENTS FROM PDF:
        // 1. Avg Age of employees with salary > 70000
        // 2. Concatenated string of at most 10 names
        // 3. Ordered by Department ID Descending

        return "SELECT " +
                "d.DEPARTMENT_NAME, " +
                "AVG(TIMESTAMPDIFF(YEAR, e.DOB, NOW())) AS AVERAGE_AGE, " +
                "SUBSTRING_INDEX(GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) SEPARATOR ', '), ', ', 10) AS EMPLOYEE_LIST " +
                "FROM EMPLOYEE e " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
                "WHERE p.AMOUNT > 70000 " +
                "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME " +
                "ORDER BY d.DEPARTMENT_ID DESC;";
    }
}