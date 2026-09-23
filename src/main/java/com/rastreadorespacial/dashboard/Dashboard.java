package com.rastreadorespacial.dashboard;

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
import com.rastreadorespacial.domain.RiskLevel;
import com.rastreadorespacial.domain.Satellite;
import com.rastreadorespacial.domain.SnapshotFormatter;
import com.rastreadorespacial.domain.SpaceObject;
import com.rastreadorespacial.domain.TrackedObjectRegistry;
import com.rastreadorespacial.risk.RiskRanking;

import java.io.PrintStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** Orquestra a coleta, agregação e exibição do painel diário. */
public final class Dashboard {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SpaceDataService dataService;
    private final SpaceDataClient neoWsClient;
    private final SpaceDataClient apodClient;
    private final SpaceDataClient celestrakClient;
    private final SpaceDataClient sbdbClient;
    private final PrintStream output;
    private final TrackedObjectRegistry registry;

    public Dashboard() {
        this(new SpaceDataService(), new NeoWsClient(), new ApodClient(), new CelestrakClient(), new SbdbClient(), System.out);
    }

    public Dashboard(SpaceDataService dataService,
                     SpaceDataClient neoWsClient,
                     SpaceDataClient apodClient,
                     SpaceDataClient celestrakClient,
                     PrintStream output) {
        this(dataService, neoWsClient, apodClient, celestrakClient, () -> "[]", output);
    }

    public Dashboard(SpaceDataService dataService,
                     SpaceDataClient neoWsClient,
                     SpaceDataClient apodClient,
                     SpaceDataClient celestrakClient,
                     SpaceDataClient sbdbClient,
                     PrintStream output) {
        this.dataService = Objects.requireNonNull(dataService, "O serviço de dados não pode ser nulo.");
        this.neoWsClient = Objects.requireNonNull(neoWsClient, "O cliente NeoWs não pode ser nulo.");
        this.apodClient = Objects.requireNonNull(apodClient, "O cliente APOD não pode ser nulo.");
        this.celestrakClient = Objects.requireNonNull(celestrakClient, "O cliente Celestrak não pode ser nulo.");
        this.sbdbClient = Objects.requireNonNull(sbdbClient, "O cliente SBDB não pode ser nulo.");
        this.output = Objects.requireNonNull(output, "A saída do dashboard não pode ser nula.");
        this.registry = new TrackedObjectRegistry();
    }

    /** Busca as fontes, atualiza o registro e exibe o painel completo. */
    public void exibir() {
        LocalDate data = LocalDate.now();
        List<String> avisosCache = new ArrayList<>();
        boolean dadosDisponiveis = false;
        ApodData apod = null;

        try {
            CachedDataResult result = dataService.fetch("neows", neoWsClient);
            List<Asteroid> asteroides = SpaceDataParser.parseNeoWs(result.rawJson());
            registry.adicionarTodos(asteroides);
            dadosDisponiveis |= !asteroides.isEmpty();
            registrarAvisoDeCache(result, "asteroides", avisosCache);
        } catch (SpaceApiException e) {
            output.println("[ERRO] NeoWs indisponível: " + e.getMessage());
        }

        try {
            CachedDataResult result = dataService.fetch("apod", apodClient);
            apod = parseApod(result.rawJson());
            dadosDisponiveis |= apod != null;
            registrarAvisoDeCache(result, "APOD", avisosCache);
        } catch (SpaceApiException e) {
            output.println("[ERRO] APOD indisponível: " + e.getMessage());
        }

        try {
            CachedDataResult result = dataService.fetch("celestrak", celestrakClient);
            List<Satellite> satelites = SpaceDataParser.parseCelestrak(result.rawJson());
            registry.adicionarTodos(satelites);
            dadosDisponiveis |= !satelites.isEmpty();
            registrarAvisoDeCache(result, "satélites", avisosCache);
        } catch (SpaceApiException e) {
            output.println("[ERRO] Celestrak indisponível: " + e.getMessage());
        }

        try {
            CachedDataResult result = dataService.fetch("sbdb", sbdbClient);
            List<Comet> cometas = SpaceDataParser.parseSbdb(result.rawJson());
            registry.adicionarTodos(cometas);
            dadosDisponiveis |= !cometas.isEmpty();
            registrarAvisoDeCache(result, "cometas", avisosCache);
        } catch (SpaceApiException e) {
            output.println("[ERRO] SBDB indisponível: " + e.getMessage());
        }

        if (!dadosDisponiveis) {
            output.println("Não há dados disponíveis para exibir hoje: nenhuma fonte respondeu e nenhum cache pôde ser usado.");
            return;
        }

        DailySnapshot snapshot = new DailySnapshot(data, registry.listarTodos());
        output.println();
        output.println("=========================================");
        output.println(" PAINEL ESPACIAL - " + data.format(DATE_FORMATTER));
        output.println("=========================================");
        for (String aviso : avisosCache) {
            output.println("[AVISO] " + aviso);
        }
        output.println("Legenda: " + legendaDeRisco());
        output.println(SnapshotFormatter.resumoTextual(snapshot));
        output.println();

        imprimirObjetos(snapshot);
        imprimirContagens(snapshot);
        imprimirApod(apod);
    }

    private void imprimirObjetos(DailySnapshot snapshot) {
        output.println("Objetos rastreados:");
        List<SpaceObject> objetosOrdenados = RiskRanking.ordenarPorPerigo(snapshot.getObjetos());
        if (objetosOrdenados.isEmpty()) {
            output.println(" - Nenhum objeto espacial rastreado.");
            return;
        }
        for (SpaceObject objeto : objetosOrdenados) {
            output.printf(" %-8s %s%n", "[" + objeto.avaliarRisco().name() + "]", descricaoObjeto(objeto));
        }
    }

    private void imprimirContagens(DailySnapshot snapshot) {
        Map<RiskLevel, Long> contagens = snapshot.contagemPorNivelDeRisco();
        output.println();
        output.printf("Contagem por nível: CRITICO: %d | ALTO: %d | MEDIO: %d | BAIXO: %d%n",
                contagens.get(RiskLevel.CRITICO), contagens.get(RiskLevel.ALTO),
                contagens.get(RiskLevel.MEDIO), contagens.get(RiskLevel.BAIXO));
    }

    private void imprimirApod(ApodData apod) {
        output.println();
        output.println("APOD do dia:");
        if (apod == null) {
            output.println(" Dados do APOD indisponíveis.");
            return;
        }
        output.println(" Título: " + apod.title());
        output.println(" Data: " + apod.date());
        output.println(" Explicação: " + apod.explanation());
        if (!apod.url().isBlank()) {
            output.println(" URL da imagem: " + apod.url());
        }
    }

    private String descricaoObjeto(SpaceObject objeto) {
        if (objeto instanceof Asteroid asteroid) {
            return String.format(Locale.US, "Asteroide %s — %.1f distâncias lunares",
                    asteroid.getNome(), asteroid.getDistanciaDePassagem().emDistanciasLunares());
        }
        if (objeto instanceof Comet comet) {
            String nucleo = comet.getDiametroNucleoKm() == null
                    ? "N/D"
                    : String.format(Locale.US, "%.1f", comet.getDiametroNucleoKm());
            return String.format("Cometa %s — núcleo %s km", comet.getNome(), nucleo);
        }
        if (objeto instanceof Satellite satellite) {
            String decaimento = satellite.getDecaimentoOrbitalDiario() == null
                    ? "N/D"
                    : String.format(Locale.US, "%.2f", satellite.getDecaimentoOrbitalDiario());
            return String.format("Satélite %s (NORAD %s) — decaimento %s km/dia",
                    satellite.getNome(), satellite.getId(), decaimento);
        }
        return objeto.getNome();
    }

    private String legendaDeRisco() {
        return String.format("[CRITICO] %s | [ALTO] %s | [MEDIO] %s | [BAIXO] %s",
                RiskLevel.CRITICO.getDescricao(), RiskLevel.ALTO.getDescricao(),
                RiskLevel.MEDIO.getDescricao(), RiskLevel.BAIXO.getDescricao());
    }

    private void registrarAvisoDeCache(CachedDataResult result, String fonte, List<String> avisosCache) {
        if (result.fromCache()) {
            avisosCache.add(String.format("Dados de %s indisponíveis hoje — exibindo cache de %s.",
                    fonte, result.dataDoArquivo()));
        }
    }

    private ApodData parseApod(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return null;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(rawJson);
            String title = root.path("title").asText("");
            String date = root.path("date").asText("");
            String explanation = root.path("explanation").asText("");
            String url = root.path("hdurl").asText(root.path("url").asText(""));
            if (title.isBlank() && date.isBlank() && explanation.isBlank() && url.isBlank()) {
                return null;
            }
            return new ApodData(title, date, explanation, url);
        } catch (IOException e) {
            output.println("[AVISO] Não foi possível interpretar os metadados do APOD.");
            return null;
        }
    }

    private record ApodData(String title, String date, String explanation, String url) {}
}
