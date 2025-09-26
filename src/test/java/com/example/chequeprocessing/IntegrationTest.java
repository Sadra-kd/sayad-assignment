package com.example.chequeprocessing;

import com.example.chequeprocessing.domain.Account;
import com.example.chequeprocessing.domain.AccountStatus;
import com.example.chequeprocessing.domain.Cheque;
import com.example.chequeprocessing.domain.ChequeStatus;
import com.example.chequeprocessing.repository.AccountRepository;
import com.example.chequeprocessing.repository.ChequeRepository;
import com.example.chequeprocessing.security.JwtUtil;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.http.client.ClientHttpResponse;
import java.io.IOException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {
    @LocalServerPort
    int port;

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ChequeRepository chequeRepository;
    @Autowired
    JwtUtil jwtUtil;

    RestTemplate rest = new RestTemplate();

    static WireMockServer wiremock;

    @DynamicPropertySource
    static void sayadBaseUrl(DynamicPropertyRegistry registry) {
        if (wiremock == null) {
            wiremock = new WireMockServer(0);
            wiremock.start();
            wiremock.stubFor(WireMock.post(WireMock.urlEqualTo("/sayad/register")).willReturn(WireMock.aResponse().withStatus(200)));
            wiremock.stubFor(WireMock.post(WireMock.urlEqualTo("/sayad/present")).willReturn(WireMock.aResponse().withStatus(200)));
        }
        registry.add("sayad.base-url", () -> "http://localhost:" + wiremock.port() + "/sayad");
    }

    String token;

    @BeforeEach
    void auth() {
        // Do not throw on 4xx so we can assert 409 responses
        rest.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });
        // Generate JWT token directly for testing
        token = jwtUtil.generateToken("teller1", "ROLE_TELLER");
        assertNotNull(token);
    }


    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void issue_then_present_paid() {
        Account a = accountRepository.findAll().stream().filter(ac -> ac.getBalance().compareTo(new BigDecimal("300000")) >= 0).findFirst().orElseThrow();

        String body = "{\"drawerId\":" + a.getId() + ",\"number\":\"YT-2025-0001\",\"amount\":150000.00}";
        ResponseEntity<Cheque> issued = rest.exchange("http://localhost:" + port + "/api/cheques", HttpMethod.POST, new HttpEntity<>(body, authHeaders()), Cheque.class);
        assertEquals(HttpStatus.CREATED, issued.getStatusCode());

        Cheque cheque = issued.getBody();
        assertNotNull(cheque);

        ResponseEntity<Cheque> presented = rest.exchange("http://localhost:" + port + "/api/cheques/" + cheque.getId() + "/present", HttpMethod.POST, new HttpEntity<>(null, authHeaders()), Cheque.class);
        assertEquals(HttpStatus.OK, presented.getStatusCode());
        assertEquals(ChequeStatus.PAID, presented.getBody().getStatus());
    }

    @Test
    void bounce_then_block_after_three() {
        // Choose an account with enough funds to issue small cheques
        Account a = accountRepository.findAll().stream().filter(ac -> ac.getBalance().compareTo(new BigDecimal("1000")) >= 0).findFirst().orElseThrow();

        for (int i = 1; i <= 3; i++) {
            String number = "YT-2025-20" + i;
            String body = "{\"drawerId\": " + a.getId() + ", \"number\": \"" + number + "\", \"amount\": 500.00}";
            // Ensure enough funds for issuance
            Account toTopUp = accountRepository.findById(a.getId()).orElseThrow();
            toTopUp.setBalance(new BigDecimal("1000.00"));
            accountRepository.save(toTopUp);

            ResponseEntity<Cheque> issued = rest.exchange("http://localhost:" + port + "/api/cheques", HttpMethod.POST, new HttpEntity<>(body, authHeaders()), Cheque.class);
            Cheque cheque = issued.getBody();

            // Force insufficient funds before presenting
            Account toDrain = accountRepository.findById(a.getId()).orElseThrow();
            toDrain.setBalance(new BigDecimal("100.00"));
            accountRepository.save(toDrain);

            ResponseEntity<String> presented = rest.exchange("http://localhost:" + port + "/api/cheques/" + cheque.getId() + "/present", HttpMethod.POST, new HttpEntity<>(null, authHeaders()), String.class);
            assertEquals(HttpStatus.CONFLICT, presented.getStatusCode());
        }

        Account updated = accountRepository.findById(a.getId()).orElseThrow();
        assertEquals(AccountStatus.BLOCKED, updated.getStatus());
    }

    @Test
    void reject_stale_cheque_over_6_months() {
        // Use high-balance account so issuance succeeds
        Account a = accountRepository.findAll().stream().filter(ac -> ac.getBalance().compareTo(new BigDecimal("300000")) >= 0).findFirst().orElseThrow();

        String body = "{\"drawerId\": " + a.getId() + ", \"number\": \"YT-2025-STALE\", \"amount\": 1000.00}";
        ResponseEntity<Cheque> issued = rest.exchange("http://localhost:" + port + "/api/cheques", HttpMethod.POST, new HttpEntity<>(body, authHeaders()), Cheque.class);
        Cheque cheque = issued.getBody();

        // Backdate the cheque to make it stale (> 6 months)
        Cheque toBackdate = chequeRepository.findById(cheque.getId()).orElseThrow();
        toBackdate.setIssueDate(LocalDate.now().minusMonths(7));
        chequeRepository.save(toBackdate);

        ResponseEntity<String> presented = rest.exchange("http://localhost:" + port + "/api/cheques/" + cheque.getId() + "/present", HttpMethod.POST, new HttpEntity<>(null, authHeaders()), String.class);
        // Assert rejection occurred (either 4xx or 5xx)
        assertTrue(presented.getStatusCode().is4xxClientError() || presented.getStatusCode().is5xxServerError());
    }
}


