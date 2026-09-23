package com.rastreadorespacial.api;

/**
 * Interface comum para clientes de busca de dados espaciais brutos (JSON).
 */
public interface SpaceDataClient {

    /**
     * Realiza a requisição HTTP e retorna o corpo bruto da resposta (JSON) como String.
     *
     * @return corpo da resposta em formato JSON
     * @throws SpaceApiException caso ocorra falha de rede, timeout ou código de status HTTP diferente de 2xx
     */
    String fetchRaw() throws SpaceApiException;
}

