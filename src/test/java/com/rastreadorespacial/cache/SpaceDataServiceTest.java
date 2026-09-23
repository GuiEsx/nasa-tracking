package com.rastreadorespacial.cache;

import com.rastreadorespacial.api.RetryPolicy;
import com.rastreadorespacial.api.SpaceApiException;
import com.rastreadorespacial.api.SpaceDataClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SpaceDataServiceTest {

    @TempDir
    Path tempDir;

    private RawDataStore rawDataStore;
    private RetryPolicy fastRetryPolicy;
    private SpaceDataService service;

    @BeforeEach
    void setUp() {
        rawDataStore = new RawDataStore(tempDir);
        // RetryPolicy instantânea para não atrasar a execução dos testes
        fastRetryPolicy = new RetryPolicy(Duration.ofMillis(1), millis -> {});
        service = new SpaceDataService(rawDataStore, fastRetryPolicy, 3);
    }

    @Test
    void testFetchSuccessUsesApiAndSavesToDisk() throws SpaceApiException {
        String expectedJson = "{\"neo_count\": 42}";
        SpaceDataClient mockSuccessClient = () -> expectedJson;

        CachedDataResult result = service.fetch("neows", mockSuccessClient);

        assertNotNull(result);
        assertEquals("neows", result.source());
        assertEquals(expectedJson, result.rawJson());
        assertFalse(result.fromCache(), "Quando a API responde, fromCache deve ser false");
        assertEquals(1, result.attempts());
        assertEquals(LocalDate.now(), result.dataDoArquivo());

        // Verifica se o arquivo foi realmente persistido no disco
        Path savedFile = rawDataStore.resolvePath("neows", LocalDate.now());
        assertTrue(Files.exists(savedFile), "O arquivo deve ter sido salvo no disco");
    }

    @Test
    void testFetchSuccessAfterRetryOn429() throws SpaceApiException {
        String expectedJson = "{\"apod\": \"success\"}";
        AtomicInteger attempts = new AtomicInteger(0);
        SpaceDataClient retryClient = () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new SpaceApiException(429, "Rate limit temporário");
            }
            return expectedJson;
        };

        CachedDataResult result = service.fetch("apod", retryClient);

        assertFalse(result.fromCache());
        assertEquals(3, result.attempts(), "Deve registrar 3 tentativas");
        assertEquals(expectedJson, result.rawJson());
    }

    @Test
    void testFetchFailureFallbackToMostRecentCacheAfterExhausting429() throws IOException, SpaceApiException {
        Path apodDir = tempDir.resolve("apod");
        Files.createDirectories(apodDir);
        Files.writeString(apodDir.resolve("2026-09-18.json"), "{\"title\": \"Foto Antiga\"}");
        Files.writeString(apodDir.resolve("2026-09-21.json"), "{\"title\": \"Foto Recente\"}");

        SpaceDataClient mockFailingClient = () -> {
            throw new SpaceApiException(429, "Rate limit persistente");
        };

        CachedDataResult result = service.fetch("apod", mockFailingClient);

        assertNotNull(result);
        assertEquals("apod", result.source());
        assertEquals("{\"title\": \"Foto Recente\"}", result.rawJson());
        assertTrue(result.fromCache(), "Em caso de falha da API com cache disponível, fromCache deve ser true");
        assertTrue(result.persistentRateLimit(), "Deve sinalizar rate limit persistente");
        assertEquals(3, result.attempts());
        assertEquals(LocalDate.of(2026, 9, 21), result.dataDoArquivo());
    }

    @Test
    void testFetchFailureFallbackToCacheOnNon429Error() throws IOException, SpaceApiException {
        Path neowsDir = tempDir.resolve("neows");
        Files.createDirectories(neowsDir);
        Files.writeString(neowsDir.resolve("2026-09-20.json"), "{\"status\": \"cached\"}");

        SpaceDataClient mock500Client = () -> {
            throw new SpaceApiException(500, "Internal Server Error");
        };

        CachedDataResult result = service.fetch("neows", mock500Client);

        assertTrue(result.fromCache());
        assertFalse(result.persistentRateLimit(), "Erro 500 não deve marcar rate limit persistente");
        assertEquals(LocalDate.of(2026, 9, 20), result.dataDoArquivo());
    }

    @Test
    void testFetchFailureWithoutCacheThrowsException() {
        SpaceDataClient mockFailingClient = () -> {
            throw new SpaceApiException(429, "Rate Limit Exceeded");
        };

        SpaceApiException exception = assertThrows(SpaceApiException.class, () ->
                service.fetch("celestrak", mockFailingClient)
        );

        assertTrue(exception.getMessage().contains("celestrak"));
        assertTrue(exception.getMessage().contains("nenhum cache local"));
    }

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> service.fetch(null, () -> "{}"));
        assertThrows(IllegalArgumentException.class, () -> service.fetch("", () -> "{}"));
        assertThrows(IllegalArgumentException.class, () -> service.fetch("neows", null));
    }
}
