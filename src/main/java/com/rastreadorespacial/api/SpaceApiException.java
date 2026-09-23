package com.rastreadorespacial.api;

/**
 * Exceção personalizada para representar erros de comunicação de rede ou status HTTP inválidos
 * nas chamadas às APIs espaciais.
 *
 * Optou-se por uma exceção checada (checked exception) pois a assinatura do método {@link SpaceDataClient#fetchRaw()}
 * requer explicitamente o tratamento de falhas de I/O e de rede por parte dos chamadores,
 * facilitando a futura implementação de mecanismos de resiliência, retry e fallbacks de cache.
 */
public class SpaceApiException extends Exception {

    private final int statusCode;

    public SpaceApiException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public SpaceApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public SpaceApiException(int statusCode, String message) {
        super(String.format("Erro HTTP %d: %s", statusCode, message));
        this.statusCode = statusCode;
    }

    public SpaceApiException(int statusCode, String message, Throwable cause) {
        super(String.format("Erro HTTP %d: %s", statusCode, message), cause);
        this.statusCode = statusCode;
    }

    /**
     * Retorna o código de status HTTP retornado pelo servidor, ou -1 caso seja um erro de conexão/timeout.
     *
     * @return código de status HTTP ou -1
     */
    public int getStatusCode() {
        return statusCode;
    }
}

