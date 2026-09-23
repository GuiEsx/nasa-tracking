package com.rastreadorespacial.api;

import com.rastreadorespacial.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Cliente para a API NASA NeoWs (Near Earth Object Web Service).
 * Busca o feed de asteroides próximos da Terra para a data atual.
 */
public class NeoWsClient implements SpaceDataClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final HttpClient httpClient;
    private final String apiKey;

    public NeoWsClient() {
        this(AppConfig.getNasaApiKey() != null && !AppConfig.getNasaApiKey().isBlank()
                ? AppConfig.getNasaApiKey()
                : "DEMO_KEY");
    }

    public NeoWsClient(String apiKey) {
        this(apiKey, HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build());
    }

    public NeoWsClient(String apiKey, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    @Override
    public String fetchRaw() throws SpaceApiException {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String uriString = String.format("%s?start_date=%s&end_date=%s&api_key=%s",
                AppConfig.NEOWS_BASE_URL, today, today, apiKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uriString))
                .timeout(TIMEOUT)
                .GET()
                .header("Accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();

            if (statusCode < 200 || statusCode >= 300) {
                throw new SpaceApiException(statusCode, "Falha na requisição para NASA NeoWs: " + response.body());
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new SpaceApiException("Erro de conexão com NASA NeoWs: " + e.getMessage(), e);
        }
    }
}

