package com.rastreadorespacial.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rastreadorespacial.config.AppConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/** Cliente da NASA/JPL SBDB para dados orbitais de cometas conhecidos. */
public class SbdbClient implements SpaceDataClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<String> COMETAS_PADRAO = List.of("1P", "2P", "9P", "19P", "67P", "109P", "C/2020 F3");

    private final HttpClient httpClient;
    private final List<String> designacoes;

    public SbdbClient() {
        this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), COMETAS_PADRAO);
    }

    public SbdbClient(HttpClient httpClient, List<String> designacoes) {
        this.httpClient = httpClient;
        this.designacoes = List.copyOf(designacoes);
    }

    @Override
    public String fetchRaw() throws SpaceApiException {
        ArrayNode respostas = OBJECT_MAPPER.createArrayNode();
        SpaceApiException ultimaFalha = null;
        for (String designacao : designacoes) {
            try {
                respostas.add(fetchCometa(designacao));
            } catch (SpaceApiException e) {
                ultimaFalha = e;
            }
        }
        if (respostas.isEmpty() && ultimaFalha != null) {
            throw ultimaFalha;
        }
        return respostas.toString();
    }

    private JsonNode fetchCometa(String designacao) throws SpaceApiException {
        String query = URLEncoder.encode(designacao, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.SBDB_BASE_URL + "?sstr=" + query + "&phys-par=1&full-prec=1"))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SpaceApiException(response.statusCode(), "Falha na requisição para NASA SBDB: " + response.body());
            }
            return OBJECT_MAPPER.readTree(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new SpaceApiException("Erro de conexão com NASA SBDB: " + e.getMessage(), e);
        }
    }
}