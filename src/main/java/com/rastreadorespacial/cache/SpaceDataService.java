package com.rastreadorespacial.cache;

import com.rastreadorespacial.api.RetryPolicy;
import com.rastreadorespacial.api.SpaceApiException;
import com.rastreadorespacial.api.SpaceDataClient;
import com.rastreadorespacial.config.AppConfig;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Serviço responsável por orquestrar a busca de dados espaciais via cliente de API,
 * aplicando política de retry com backoff exponencial para rate limit (HTTP 429)
 * e fallback automático para cache local em caso de falhas persistentes.
 */
public class SpaceDataService {

    private final RawDataStore rawDataStore;
    private final RetryPolicy retryPolicy;
    private final int maxAttempts;

    public SpaceDataService() {
        this(new RawDataStore(), new RetryPolicy(), AppConfig.MAX_RETRY_ATTEMPTS);
    }

    public SpaceDataService(RawDataStore rawDataStore) {
        this(rawDataStore, new RetryPolicy(), AppConfig.MAX_RETRY_ATTEMPTS);
    }

    public SpaceDataService(RawDataStore rawDataStore, RetryPolicy retryPolicy, int maxAttempts) {
        this.rawDataStore = rawDataStore;
        this.retryPolicy = retryPolicy;
        this.maxAttempts = maxAttempts > 0 ? maxAttempts : AppConfig.MAX_RETRY_ATTEMPTS;
    }

    /**
     * Busca dados da fonte especificada através do cliente fornecido, com suporte a retry em caso de 429
     * e fallback para cache local em caso de falha.
     *
     * @param source nome da fonte ("neows", "apod", "celestrak")
     * @param client cliente de API para realizar a busca remota
     * @return CachedDataResult com o JSON e metadados de origem e tentativas
     * @throws SpaceApiException se a API falhar e não houver cache local disponível
     */
    public CachedDataResult fetch(String source, SpaceDataClient client) throws SpaceApiException {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("A fonte (source) não pode ser nula ou vazia.");
        }
        if (client == null) {
            throw new IllegalArgumentException("O cliente (SpaceDataClient) não pode ser nulo.");
        }

        try {
            RetryPolicy.RetryResult<String> retryResult = retryPolicy.executeWithRetryDetailed(
                    client::fetchRaw,
                    maxAttempts,
                    source
            );

            String rawJson = retryResult.result();
            rawDataStore.save(source, rawJson);

            return new CachedDataResult(
                    source,
                    rawJson,
                    false,
                    LocalDate.now(),
                    retryResult.totalAttempts(),
                    false
            );
        } catch (SpaceApiException e) {
            boolean isPersistent429 = e.getStatusCode() == 429;
            Optional<CachedDataResult> cachedOptional = rawDataStore.loadMostRecent(source);

            if (cachedOptional.isPresent()) {
                CachedDataResult cached = cachedOptional.get();
                String motivo = isPersistent429
                        ? "429 Rate Limit persistente após tentativas"
                        : e.getMessage();

                System.out.println(String.format(
                        "[AVISO] Falha ao buscar %s da API (%s), usando cache local de %s",
                        source, motivo, cached.dataDoArquivo()
                ));

                return new CachedDataResult(
                        cached.source(),
                        cached.rawJson(),
                        true,
                        cached.dataDoArquivo(),
                        maxAttempts,
                        isPersistent429
                );
            }

            throw new SpaceApiException(
                    String.format("Falha ao buscar %s da API e nenhum cache local disponível. Erro: %s", source, e.getMessage()),
                    e
            );
        }
    }
}
