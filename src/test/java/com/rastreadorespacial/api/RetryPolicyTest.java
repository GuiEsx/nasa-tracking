package com.rastreadorespacial.api;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void testRetrySucceedsAfterTwoRateLimits() throws SpaceApiException {
        List<Long> sleepDurations = new ArrayList<>();
        RetryPolicy.Sleeper testSleeper = sleepDurations::add;
        RetryPolicy retryPolicy = new RetryPolicy(Duration.ofSeconds(2), testSleeper);

        AtomicInteger callCount = new AtomicInteger(0);
        String expectedResult = "Sucesso";

        RetryPolicy.RetryResult<String> result = retryPolicy.executeWithRetryDetailed(() -> {
            int current = callCount.incrementAndGet();
            if (current == 1) {
                throw new SpaceApiException(429, "Rate Limit 1");
            } else if (current == 2) {
                throw new SpaceApiException(429, "Rate Limit 2");
            }
            return expectedResult;
        }, 3, "teste-429");

        assertEquals(expectedResult, result.result());
        assertEquals(3, result.totalAttempts(), "Deve ter executado 3 tentativas");
        assertEquals(3, callCount.get());

        // Validação do backoff exponencial: 2000ms na 1ª falha, 4000ms na 2ª falha
        assertEquals(2, sleepDurations.size());
        assertEquals(2000L, sleepDurations.get(0));
        assertEquals(4000L, sleepDurations.get(1));
    }

    @Test
    void testRetryExhaustsMaxAttemptsOn429() {
        List<Long> sleepDurations = new ArrayList<>();
        RetryPolicy.Sleeper testSleeper = sleepDurations::add;
        RetryPolicy retryPolicy = new RetryPolicy(Duration.ofSeconds(2), testSleeper);

        AtomicInteger callCount = new AtomicInteger(0);

        SpaceApiException exception = assertThrows(SpaceApiException.class, () ->
                retryPolicy.executeWithRetryDetailed(() -> {
                    callCount.incrementAndGet();
                    throw new SpaceApiException(429, "Rate limit persistente");
                }, 3, "teste-esgotamento")
        );

        assertEquals(429, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Limite de 3 tentativas excedido"));
        assertEquals(3, callCount.get(), "Deve ter tentado exatamente 3 vezes");
        assertEquals(2, sleepDurations.size(), "Deve ter dormido 2 vezes (antes da tentativa 2 e antes da 3)");
        assertEquals(2000L, sleepDurations.get(0));
        assertEquals(4000L, sleepDurations.get(1));
    }

    @Test
    void testNon429ErrorFailsImmediatelyWithoutRetryOrSleep() {
        List<Long> sleepDurations = new ArrayList<>();
        RetryPolicy.Sleeper testSleeper = sleepDurations::add;
        RetryPolicy retryPolicy = new RetryPolicy(Duration.ofSeconds(2), testSleeper);

        AtomicInteger callCount = new AtomicInteger(0);

        SpaceApiException exception = assertThrows(SpaceApiException.class, () ->
                retryPolicy.executeWithRetry(() -> {
                    callCount.incrementAndGet();
                    throw new SpaceApiException(500, "Internal Server Error");
                }, 3, "teste-500")
        );

        assertEquals(500, exception.getStatusCode());
        assertEquals(1, callCount.get(), "Deve ter executado apenas 1 vez");
        assertTrue(sleepDurations.isEmpty(), "Não deve dormir nem tentar novamente para erros diferentes de 429");
    }

    @Test
    void testNetworkTimeoutFailsImmediatelyWithoutRetry() {
        List<Long> sleepDurations = new ArrayList<>();
        RetryPolicy.Sleeper testSleeper = sleepDurations::add;
        RetryPolicy retryPolicy = new RetryPolicy(Duration.ofSeconds(2), testSleeper);

        AtomicInteger callCount = new AtomicInteger(0);

        SpaceApiException exception = assertThrows(SpaceApiException.class, () ->
                retryPolicy.executeWithRetry(() -> {
                    callCount.incrementAndGet();
                    throw new SpaceApiException("Connection Timeout");
                }, 3, "teste-timeout")
        );

        assertEquals(-1, exception.getStatusCode());
        assertEquals(1, callCount.get());
        assertTrue(sleepDurations.isEmpty());
    }
}

