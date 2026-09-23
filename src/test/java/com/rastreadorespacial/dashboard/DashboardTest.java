package com.rastreadorespacial.dashboard;

import com.rastreadorespacial.api.SpaceApiException;
import com.rastreadorespacial.api.SpaceDataClient;
import com.rastreadorespacial.cache.CachedDataResult;
import com.rastreadorespacial.cache.RawDataStore;
import com.rastreadorespacial.cache.SpaceDataService;
import com.rastreadorespacial.api.RetryPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardTest {

    @TempDir
    Path tempDir;

    @Test
    void exibeTresFontesOrdenadasESemAvisoDeCache() {
        FakeSpaceDataService service = new FakeSpaceDataService(tempDir, Map.of(
                "neows", result("neows", """
                                                {"near_earth_objects":{"2026-09-23":[
                                                    {"id":"1","name":"Asteroide Baixo","is_potentially_hazardous_asteroid":false,
                                                     "close_approach_data":[{"miss_distance":{"lunar":"40.0"}}]},
                                                    {"id":"2","name":"Asteroide Alto","is_potentially_hazardous_asteroid":true,
                                                     "close_approach_data":[{"miss_distance":{"lunar":"3.0"}}]}
                        ]}}
                        """),
                "apod", result("apod", """
                        {"title":"Lua sobre Marte","date":"2026-09-23",
                         "explanation":"Uma explicação de teste.","url":"https://example.test/apod.jpg"}
                        """),
                "celestrak", result("celestrak", """
                        [{"NORAD_CAT_ID":"25544","OBJECT_NAME":"ISS","BSTAR":0.02,
                          "INCLINATION":51.6,"ECCENTRICITY":0.0005,"MEAN_MOTION":15.5}]
                        """)));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        new Dashboard(service, client(), client(), client(), new PrintStream(bytes)).exibir();

        String output = bytes.toString();
        assertFalse(output.contains("[AVISO] Dados de"));
        assertTrue(output.indexOf("[ALTO]") < output.indexOf("[BAIXO]"));
        assertTrue(output.contains("Contagem por nível: CRITICO: 1 | ALTO: 1 | MEDIO: 0 | BAIXO: 1"));
        assertTrue(output.contains("Título: Lua sobre Marte"));
        assertTrue(output.contains("URL da imagem: https://example.test/apod.jpg"));
    }

    @Test
    void exibeAvisoQuandoUmaFonteUsaCache() {
        FakeSpaceDataService service = new FakeSpaceDataService(tempDir, Map.of(
                "neows", result("neows", "{\"near_earth_objects\":{}}"),
                "apod", result("apod", "{\"title\":\"APOD em cache\",\"date\":\"2026-09-20\",\"explanation\":\"Texto\"}"),
                "celestrak", new CachedDataResult("celestrak", "[]", true, LocalDate.of(2026, 9, 20))));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        new Dashboard(service, client(), client(), client(), new PrintStream(bytes)).exibir();

        String output = bytes.toString();
        assertTrue(output.contains("[AVISO] Dados de satélites indisponíveis hoje — exibindo cache de 2026-09-20."));
        assertTrue(output.contains("APOD em cache"));
    }

    @Test
    void informaAusenciaTotalDeDadosSemLancarExcecao() {
        FakeSpaceDataService service = new FakeSpaceDataService(tempDir, Map.of());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        new Dashboard(service, client(), client(), client(), new PrintStream(bytes)).exibir();

        String output = bytes.toString();
        assertTrue(output.contains("Não há dados disponíveis para exibir hoje"));
    }

    private static CachedDataResult result(String source, String json) {
        return new CachedDataResult(source, json, false, LocalDate.now());
    }

    private static SpaceDataClient client() {
        return () -> "{}";
    }

    private static final class FakeSpaceDataService extends SpaceDataService {
        private final Map<String, CachedDataResult> results;

        private FakeSpaceDataService(Path tempDir, Map<String, CachedDataResult> results) {
            super(new RawDataStore(tempDir), new RetryPolicy(Duration.ZERO, ignored -> {}), 1);
            this.results = results;
        }

        @Override
        public CachedDataResult fetch(String source, SpaceDataClient client) throws SpaceApiException {
            CachedDataResult result = results.get(source);
            if (result == null) {
                throw new SpaceApiException("Fonte indisponível no cenário de teste: " + source);
            }
            return result;
        }
    }
}
