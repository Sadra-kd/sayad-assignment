package com.example.chequeprocessing.sayad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class SayadClientTest {
    private RestTemplate restTemplate;
    private SayadProperties properties;
    private MockRestServiceServer server;
    private SayadClient client;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        properties = new SayadProperties();
        properties.setBaseUrl("http://localhost:8081/sayad");
        server = MockRestServiceServer.createServer(restTemplate);
        client = new SayadClient(restTemplate, properties);
    }

    @Test
    void register_ok() {
        server.expect(requestTo("http://localhost:8081/sayad/register"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        assertTrue(client.registerCheque("X"));
    }

    @Test
    void present_ok() {
        server.expect(requestTo("http://localhost:8081/sayad/present"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        assertTrue(client.presentCheque("X"));
    }
}


