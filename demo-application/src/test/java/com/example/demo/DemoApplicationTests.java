package com.example.demo;

import com.example.demo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class DemoApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCrossTenantDataIsolation() {
        // 1. Create a user in tenant1
        HttpHeaders headers1 = new HttpHeaders();
        headers1.set("X-Tenant-ID", "tenant1");
        headers1.set("Content-Type", "application/json");

        User user1 = new User();
        user1.setName("User T1");
        user1.setEmail("user1@tenant1.com");

        HttpEntity<User> request1 = new HttpEntity<>(user1, headers1);
        ResponseEntity<User> response1 = restTemplate.postForEntity("/api/users", request1, User.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. Create a user in tenant2
        HttpHeaders headers2 = new HttpHeaders();
        headers2.set("X-Tenant-ID", "tenant2");
        headers2.set("Content-Type", "application/json");

        User user2 = new User();
        user2.setName("User T2");
        user2.setEmail("user2@tenant2.com");

        HttpEntity<User> request2 = new HttpEntity<>(user2, headers2);
        ResponseEntity<User> response2 = restTemplate.postForEntity("/api/users", request2, User.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 3. Verify tenant1 only sees its own users
        HttpEntity<Void> getRequest1 = new HttpEntity<>(headers1);
        ResponseEntity<List<User>> getResponse1 = restTemplate.exchange(
                "/api/users", HttpMethod.GET, getRequest1, new ParameterizedTypeReference<List<User>>() {}
        );
        assertThat(getResponse1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse1.getBody()).hasSize(1);
        assertThat(getResponse1.getBody().get(0).getEmail()).isEqualTo("user1@tenant1.com");

        // 4. Verify tenant2 only sees its own users
        HttpEntity<Void> getRequest2 = new HttpEntity<>(headers2);
        ResponseEntity<List<User>> getResponse2 = restTemplate.exchange(
                "/api/users", HttpMethod.GET, getRequest2, new ParameterizedTypeReference<List<User>>() {}
        );
        assertThat(getResponse2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse2.getBody()).hasSize(1);
        assertThat(getResponse2.getBody().get(0).getEmail()).isEqualTo("user2@tenant2.com");
    }

    @Test
    public void testMissingTenantHeaderReturns400() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("X-Tenant-ID header is missing");
    }

    @Test
    public void testInvalidTenantHeaderReturns404() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "invalid-tenant");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/users", HttpMethod.GET, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Tenant not found: invalid-tenant");
    }

    @Test
    public void testTenantDataCannotBeAccessedByAnotherTenant() {
        // 1. Create a user in tenant1
        HttpHeaders headers1 = new HttpHeaders();
        headers1.set("X-Tenant-ID", "tenant1");
        headers1.set("Content-Type", "application/json");

        User user1 = new User();
        user1.setName("Secret User");
        user1.setEmail("secret@tenant1.com");

        HttpEntity<User> request1 = new HttpEntity<>(user1, headers1);
        ResponseEntity<User> response1 = restTemplate.postForEntity("/api/users", request1, User.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long userId = response1.getBody().getId();

        // 2. Query the user using tenant2
        HttpHeaders headers2 = new HttpHeaders();
        headers2.set("X-Tenant-ID", "tenant2");
        HttpEntity<Void> request2 = new HttpEntity<>(headers2);

        ResponseEntity<User> response2 = restTemplate.exchange("/api/users/" + userId, HttpMethod.GET, request2, User.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
