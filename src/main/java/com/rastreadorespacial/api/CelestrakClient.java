package com.rastreadorespacial.api;

import com.rastreadorespacial.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Cliente para a API Celestrak (dados orbitais / TLE em formato JSON).
 * Não necessita de chave de API.
 */
public class CelestrakClient implements SpaceDataClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String DEFAULT_GROUP = "stations";

    private final HttpClient httpClient;
    private final String group;

    public CelestrakClient() {
        this(DEFAULT_GROUP);
    }

    public CelestrakClient(String group) {
        this(group, HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build());
    }

    public CelestrakClient(String group, HttpClient httpClient) {
        this.group = group;
        this.httpClient = httpClient;
    }

    @Override
    public String fetchRaw() throws SpaceApiException {
        String uriString = String.format("%s?GROUP=%s&FORMAT=json", AppConfig.CELESTRAK_BASE_URL, group);

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
                throw new SpaceApiException(statusCode, "Falha na requisição para Celestrak: " + response.body());
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new SpaceApiException("Erro de conexão com Celestrak: " + e.getMessage(), e);
        }
    }
}

