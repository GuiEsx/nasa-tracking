package com.rastreadorespacial.menu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rastreadorespacial.api.ApodClient;
import com.rastreadorespacial.api.CelestrakClient;
import com.rastreadorespacial.api.NeoWsClient;
import com.rastreadorespacial.api.SbdbClient;
import com.rastreadorespacial.api.SpaceApiException;
import com.rastreadorespacial.api.SpaceDataClient;
import com.rastreadorespacial.api.SpaceDataParser;
import com.rastreadorespacial.cache.CachedDataResult;
import com.rastreadorespacial.cache.SpaceDataService;
import com.rastreadorespacial.domain.Asteroid;
import com.rastreadorespacial.domain.Comet;
import com.rastreadorespacial.domain.DailySnapshot;
import com.rastreadorespacial.domain.DistanceUnit;
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
    private DistanceUnit unidadeDistancia = DistanceUnit.KILOMETERS;
    private ApodInfo apod;

    public MenuContext() {
        this(new SpaceDataService(), new NeoWsClient(), new ApodClient(), new CelestrakClient(), new SbdbClient(), System.out);
    }

    public MenuContext(SpaceDataService dataService,
                       SpaceDataClient neoWsClient,
                       SpaceDataClient apodClient,
                       SpaceDataClient celestrakClient,
                       PrintStream output) {
                this(dataService, neoWsClient, apodClient, celestrakClient, () -> "[]", output);
                }

                public MenuContext(SpaceDataService dataService,
                           SpaceDataClient neoWsClient,
                           SpaceDataClient apodClient,
                           SpaceDataClient celestrakClient,
                           SpaceDataClient sbdbClient,
                           PrintStream output) {
        this.dataService = Objects.requireNonNull(dataService);
        this.output = Objects.requireNonNull(output);
        clients = Map.of("neows", Objects.requireNonNull(neoWsClient),
                "apod", Objects.requireNonNull(apodClient),
                    "celestrak", Objects.requireNonNull(celestrakClient),
                    "sbdb", Objects.requireNonNull(sbdbClient));

        // Carrega os feeds iniciais para que o programa não abra com o registro vazio.
        atualizarTodos(false);
    }

    public int atualizarTodos() {
        return atualizarTodos(true);
    }

    private int atualizarTodos(boolean forcarApi) {
        int antes = registry.quantidade();
        atualizar("neows", forcarApi);
        atualizar("apod", forcarApi);
        atualizar("celestrak", forcarApi);
        atualizar("sbdb", forcarApi);
        return registry.quantidade() - antes;
    }

    public boolean atualizar(String source) {
        return atualizar(source, true);
    }

    private boolean atualizar(String source, boolean forcarApi) {
        SpaceDataClient client = clients.get(source);
        if (client == null) {
            return false;
        }
        try {
            CachedDataResult result = dataService.fetch(source, client, forcarApi);
            ultimoCarregamento.put(source, result);
            if ("neows".equals(source)) {
                List<Asteroid> asteroides = SpaceDataParser.parseNeoWs(result.rawJson());
                registry.adicionarTodos(asteroides);
                output.println("NeoWs: " + asteroides.size() + " asteroide(s) carregado(s).");
            } else if ("celestrak".equals(source)) {
                List<Satellite> satelites = SpaceDataParser.parseCelestrak(result.rawJson());
                registry.adicionarTodos(satelites);
                output.println("Celestrak: " + satelites.size() + " satélite(s) carregado(s).");
            } else if ("sbdb".equals(source)) {
                List<Comet> cometas = SpaceDataParser.parseSbdb(result.rawJson());
                registry.adicionarTodos(cometas);
                output.println("SBDB: " + cometas.size() + " cometa(s) carregado(s).");
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

    public DistanceUnit unidadeDistancia() {
        return unidadeDistancia;
    }

    public void alterarUnidadeDistancia(MenuNavigator navigator) {
        output.println("Escolha a unidade de distância:");
        output.println("1 - Quilômetros");
        output.println("2 - Milhas");
        output.println("3 - Distâncias lunares");
        output.println("Digite sua opção:");

        String entrada = navigator.scanner().nextLine().trim();
        try {
            int escolha = Integer.parseInt(entrada);
            this.unidadeDistancia = DistanceUnit.fromChoice(escolha);
            output.println("Unidade de distância atualizada para: " + this.unidadeDistancia.name());
        } catch (NumberFormatException e) {
            output.println("Opção inválida. Mantendo a unidade atual: " + this.unidadeDistancia.name());
        }
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
