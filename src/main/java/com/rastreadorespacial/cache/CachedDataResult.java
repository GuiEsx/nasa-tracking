package com.rastreadorespacial.cache;

import java.time.LocalDate;

/**
 * Representa o resultado de uma busca de dados espaciais, indicando sua origem (API ou cache local),
 * a data do arquivo correspondente, o número de tentativas e se houve rate limit persistente.
 *
 * @param source nome da fonte de dados ("neows", "apod", "celestrak")
 * @param rawJson conteúdo bruto em formato JSON
 * @param fromCache true se os dados foram recuperados do cache local; false se vieram da chamada de API
 * @param dataDoArquivo data correspondente ao arquivo
 * @param attempts número de tentativas de requisição realizadas
 * @param persistentRateLimit true se o cache foi acionado após esgotamento de tentativas por HTTP 429
 */
public record CachedDataResult(
        String source,
        String rawJson,
        boolean fromCache,
        LocalDate dataDoArquivo,
        int attempts,
        boolean persistentRateLimit
) {
    public CachedDataResult(String source, String rawJson, boolean fromCache, LocalDate dataDoArquivo) {
        this(source, rawJson, fromCache, dataDoArquivo, 1, false);
    }
}
