package com.rastreadorespacial.menu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rastreadorespacial.api.ApodClient;
import com.rastreadorespacial.api.CelestrakClient;
import com.rastreadorespacial.api.NeoWsClient;
import com.rastreadorespacial.api.SpaceApiException;
import com.rastreadorespacial.api.SpaceDataClient;
import com.rastreadorespacial.api.SpaceDataParser;
import com.rastreadorespacial.cache.CachedDataResult;
import com.rastreadorespacial.cache.SpaceDataService;
import com.rastreadorespacial.domain.Asteroid;
import com.rastreadorespacial.domain.DailySnapshot;
import com.rastreadorespacial.domain.Satellite;
import com.rastreadorespacial.domain.TrackedObjectRegistry;

import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MenuContext {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SpaceDataService dataService;
    private final Map<String, SpaceDataClient> clients;
    private final Map<String, CachedDataResult> ultimoCarregamento = new LinkedHashMap<>();
    private final TrackedObjectRegistry registry = new TrackedObjectRegistry();
    private final PrintStream output;
    private ApodInfo apod;

    public MenuContext() {
        this(new SpaceDataService(), new NeoWsClient(), new ApodClient(), new CelestrakClient(), System.out);
    }

    public MenuContext(SpaceDataService dataService,
                       SpaceDataClient neoWsClient,
                       SpaceDataClient apodClient,
                       SpaceDataClient celestrakClient,
                       PrintStream output) {
        this.dataService = Objects.requireNonNull(dataService);
        this.output = Objects.requireNonNull(output);
        clients = Map.of("neows", Objects.requireNonNull(neoWsClient),
                "apod", Objects.requireNonNull(apodClient),
                "celestrak", Objects.requireNonNull(celestrakClient));
    }

    public int atualizarTodos() {
        int antes = registry.quantidade();
        atualizar("neows");
        atualizar("apod");
        atualizar("celestrak");
        return registry.quantidade() - antes;
    }

    public boolean atualizar(String source) {
        SpaceDataClient client = clients.get(source);
        if (client == null) {
            return false;
        }
        try {
            CachedDataResult result = dataService.fetch(source, client);
            ultimoCarregamento.put(source, result);
            if ("neows".equals(source)) {
                List<Asteroid> asteroides = SpaceDataParser.parseNeoWs(result.rawJson());
                registry.adicionarTodos(asteroides);
                output.println("NeoWs: " + asteroides.size() + " asteroide(s) carregado(s).");
            } else if ("celestrak".equals(source)) {
                List<Satellite> satelites = SpaceDataParser.parseCelestrak(result.rawJson());
                registry.adicionarTodos(satelites);
                output.println("Celestrak: " + satelites.size() + " satélite(s) carregado(s).");
            } else {
                apod = parseApod(result.rawJson());
                output.println("APOD: metadados carregados.");
            }
            return true;
        } catch (SpaceApiException e) {
            output.println("[ERRO] " + source + " indisponível: " + e.getMessage());
            return false;
        }
    }

    public TrackedObjectRegistry registry() {
        return registry;
    }

    public DailySnapshot snapshot() {
        return new DailySnapshot(LocalDate.now(), registry.listarTodos());
    }

    public ApodInfo apod() {
        return apod;
    }

    public Map<String, CachedDataResult> ultimoCarregamento() {
        return Map.copyOf(ultimoCarregamento);
    }

    public PrintStream output() {
        return output;
    }

    private ApodInfo parseApod(String rawJson) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(rawJson);
            return new ApodInfo(root.path("title").asText(""), root.path("date").asText(""),
                    root.path("explanation").asText(""),
                    root.path("hdurl").asText(root.path("url").asText("")));
        } catch (IOException | NullPointerException e) {
            output.println("[AVISO] Não foi possível interpretar o APOD.");
            return null;
        }
    }

    public record ApodInfo(String titulo, String data, String explicacao, String url) {}
}
