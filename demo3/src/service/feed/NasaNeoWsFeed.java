package service.feed;

import model.Asteroide;
import model.Distancia;
import service.BackupDadosService;
import service.RastreadorService;
import util.ApiHttpUtil;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NasaNeoWsFeed implements NasaFeed<List<Asteroide>> {
    private final LocalDate dataConsulta;
    private final String apiKey;

    public NasaNeoWsFeed() {
        this(LocalDate.now(), "DEMO_KEY");
    }

    public NasaNeoWsFeed(LocalDate dataConsulta, String apiKey) {
        this.dataConsulta = dataConsulta;
        this.apiKey = (apiKey != null && !apiKey.trim().isEmpty()) ? apiKey.trim() : "DEMO_KEY";
    }

    @Override
    public String getNomeFeed() {
        return "NASA NeoWs (Near Earth Object Web Service)";
    }

    @Override
    public String getEndpointUrl() {
        String dataIso = dataConsulta.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return String.format("https://api.nasa.gov/neo/rest/v1/feed?start_date=%s&end_date=%s&api_key=%s",
                dataIso, dataIso, apiKey);
    }

    @Override
    public List<Asteroide> consultarFeed() throws Exception {
        List<Asteroide> asteroides = new ArrayList<>();
        try {
            URL url = URI.create(getEndpointUrl()).toURL();
            String json = ApiHttpUtil.lerResposta(url, "Mozilla/5.0");

            int cursor = 0;
            while (true) {
                int indexId = json.indexOf("\"id\":\"", cursor);
                if (indexId == -1) break;
                indexId += 6;
                String id = json.substring(indexId, json.indexOf("\"", indexId));

                int indexNome = json.indexOf("\"name\":\"", indexId);
                if (indexNome == -1) break;
                indexNome += 8;
                String nome = json.substring(indexNome, json.indexOf("\"", indexNome));

                int indexPerigo = json.indexOf("\"is_potentially_hazardous_asteroid\":", indexNome);
                boolean perigoso = false;
                if (indexPerigo != -1) {
                    indexPerigo += 36;
                    String perigoTexto = json.substring(indexPerigo, Math.min(indexPerigo + 10, json.length()));
                    perigoso = perigoTexto.trim().startsWith("true");
                }

                int indexDist = json.indexOf("\"kilometers\":\"", indexNome);
                double distanciaKm = 15000000.0;
                if (indexDist != -1) {
                    indexDist += 14;
                    String distStr = json.substring(indexDist, json.indexOf("\"", indexDist));
                    distanciaKm = Double.parseDouble(distStr);
                }

                LocalDateTime ultimoAvistamento = LocalDateTime.now();
                int indexEpoch = json.indexOf("\"epoch_date_close_approach\":", indexNome);
                if (indexEpoch != -1) {
                    indexEpoch += 29;
                    int fimEpoch = json.indexOf(",", indexEpoch);
                    if (fimEpoch != -1) {
                        String epochStr = json.substring(indexEpoch, fimEpoch).trim();
                        try {
                            long epochMillis = Long.parseLong(epochStr);
                            ultimoAvistamento = Instant.ofEpochMilli(epochMillis)
                                    .atOffset(ZoneOffset.UTC)
                                    .toLocalDateTime();
                        } catch (Exception ignored) {
                        }
                    }
                }

                Distancia distancia = Distancia.deKilometros(distanciaKm);
                asteroides.add(new Asteroide(id, nome, distancia, perigoso, ultimoAvistamento));

                cursor = (indexEpoch != -1) ? indexEpoch : indexNome + 20;
            }

        } catch (exception.RateLimitExceededException rle) {
            System.err.println("\n[RF8 - ATENÇÃO] Limite de requisições da NASA excedido (HTTP 429) após múltiplas tentativas com backoff!");
            System.err.printf("[RF8 - Informação] O sistema aguardou e tentou novamente por %d vezes (totalizando %d s de espera) sem travar.%n",
                    rle.getTentativasRealizadas(), (rle.getTempoTotalEsperaMs() / 1000));
            System.err.println("[RF8 - Informação] Desistindo definitivamente do acesso online para proteger a estabilidade do sistema.");
            System.out.println("-> Acionando contingência: carregando asteroides do arquivo de backup local (dados_backup_objetos.txt)...");
            List<Asteroide> todosBackup = BackupDadosService.carregarTodosAsteroides();
            if (todosBackup != null && !todosBackup.isEmpty()) {
                asteroides.addAll(todosBackup);
            }
        } catch (Exception e) {
            System.out.println("[NASA NeoWs] Falha ao consultar feed online: " + e.getMessage() + ". Utilizando backup local.");
            Asteroide backup = BackupDadosService.carregarAsteroide();
            if (backup != null) {
                asteroides.add(backup);
            }
        }

        if (asteroides.isEmpty()) {
            Asteroide backup = BackupDadosService.carregarAsteroide();
            if (backup != null) {
                asteroides.add(backup);
            } else {
                asteroides.add(RastreadorService.criarAsteroideFallback());
            }
        }

        return asteroides;
    }
}

