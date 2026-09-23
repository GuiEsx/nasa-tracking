package com.rastreadorespacial.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Gerenciador de configurações do aplicativo, como chaves de API, URLs base,
 * políticas de retry e diretórios de dados.
 */
public final class AppConfig {

    public static final String NEOWS_BASE_URL = "https://api.nasa.gov/neo/rest/v1/feed";
    public static final String APOD_BASE_URL = "https://api.nasa.gov/planetary/apod";
    public static final String CELESTRAK_BASE_URL = "https://celestrak.org/NORAD/elements/gp.php";
    public static final String SBDB_BASE_URL = "https://ssd-api.jpl.nasa.gov/sbdb.api";

    public static final Path DATA_DIR = Path.of("data");
    public static final Path NEOWS_DATA_DIR = DATA_DIR.resolve("neows");
    public static final Path APOD_DATA_DIR = DATA_DIR.resolve("apod");
    public static final Path CELESTRAK_DATA_DIR = DATA_DIR.resolve("celestrak");
    public static final Path SBDB_DATA_DIR = DATA_DIR.resolve("sbdb");

    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(2);

    private static final Dotenv DOTENV;

    static {
        // Carrega o arquivo .env se existir, sem falhar caso não exista
        DOTENV = Dotenv.configure().ignoreIfMissing().load();
        initDirectories();
    }

    private AppConfig() {
        // Classe utilitária / configuração estática
    }

    /**
     * Inicializa os diretórios de cache e armazenamento local caso não existam.
     */
    public static void initDirectories() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.createDirectories(NEOWS_DATA_DIR);
            Files.createDirectories(APOD_DATA_DIR);
            Files.createDirectories(CELESTRAK_DATA_DIR);
            Files.createDirectories(SBDB_DATA_DIR);
        } catch (IOException e) {
            System.err.println("Aviso: Falha ao criar diretórios de dados locais: " + e.getMessage());
        }
    }

    /**
     * Obtém a chave de API da NASA a partir do arquivo .env ou das variáveis de ambiente do sistema.
     *
     * @return a chave de API ou null/vazio se não configurada.
     */
    public static String getNasaApiKey() {
        String key = DOTENV.get("NASA_API_KEY");
        if (key == null || key.isBlank()) {
            key = System.getenv("NASA_API_KEY");
        }
        return key;
    }
}
