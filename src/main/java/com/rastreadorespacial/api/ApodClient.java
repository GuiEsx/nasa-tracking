package com.rastreadorespacial.api;

import com.rastreadorespacial.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente para a API NASA APOD (Astronomy Picture of the Day).
 */
public class ApodClient implements SpaceDataClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final String apiKey;

    public ApodClient() {
        this(AppConfig.getNasaApiKey() != null && !AppConfig.getNasaApiKey().isBlank()
                ? AppConfig.getNasaApiKey()
                : "DEMO_KEY");
    }

    public ApodClient(String apiKey) {
        this(apiKey, HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build());
    }

    public ApodClient(String apiKey, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
    }

    @Override
    public String fetchRaw() throws SpaceApiException {
        String uriString = String.format("%s?api_key=%s", AppConfig.APOD_BASE_URL, apiKey);

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
                throw new SpaceApiException(statusCode, "Falha na requisição para NASA APOD: " + response.body());
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new SpaceApiException("Erro de conexão com NASA APOD: " + e.getMessage(), e);
        }
    }
}

