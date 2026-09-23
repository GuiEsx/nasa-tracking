package com.rastreadorespacial.api;

import com.rastreadorespacial.config.AppConfig;

import java.time.Duration;

/**
 * Política de tentativas (retry) com backoff exponencial para lidar com HTTP 429 (Rate Limit).
 *
 * <p><strong>Nota de arquitetura:</strong> Esta implementação síncrona utiliza uma abstração de {@link Sleeper}
 * (por padrão {@code Thread.sleep()}). Em uma versão futura assíncrona ou reativa, a espera poderá ser
 * delegada a um {@code CompletableFuture} em conjunto com um {@code ScheduledExecutorService}.</p>
 */
public class RetryPolicy {

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T run() throws SpaceApiException;
    }

    @FunctionalInterface
    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    public record RetryResult<T>(T result, int totalAttempts) {}

    private final Duration initialDelay;
    private final Sleeper sleeper;

    public RetryPolicy() {
        this(AppConfig.INITIAL_RETRY_DELAY, Thread::sleep);
    }

    public RetryPolicy(Duration initialDelay, Sleeper sleeper) {
        this.initialDelay = initialDelay != null ? initialDelay : AppConfig.INITIAL_RETRY_DELAY;
        this.sleeper = sleeper != null ? sleeper : Thread::sleep;
    }

    /**
     * Executa uma operação com tentativas repetidas em caso de HTTP 429 (Rate Limit).
     *
     * @param operacao operação que pode lançar {@link SpaceApiException}
     * @param maxTentativas número máximo de tentativas
     * @param <T> tipo de retorno
     * @return resultado da operação
     * @throws SpaceApiException caso ocorra outro erro HTTP, erro de conexão ou se as tentativas se esgotarem
     */
    public <T> T executeWithRetry(RetryableOperation<T> operacao, int maxTentativas) throws SpaceApiException {
        return executeWithRetry(operacao, maxTentativas, "Operação");
    }

    /**
     * Executa uma operação com tentativas repetidas em caso de HTTP 429 (Rate Limit), identificando a operação por nome.
     *
     * @param operacao operação que pode lançar {@link SpaceApiException}
     * @param maxTentativas número máximo de tentativas
     * @param operationName nome amigável da fonte ou operação para fins de log
     * @param <T> tipo de retorno
     * @return resultado da operação
     * @throws SpaceApiException caso ocorra outro erro HTTP, erro de conexão ou se as tentativas se esgotarem
     */
    public <T> T executeWithRetry(RetryableOperation<T> operacao, int maxTentativas, String operationName) throws SpaceApiException {
        return executeWithRetryDetailed(operacao, maxTentativas, operationName).result();
    }

    /**
     * Executa a operação retornando os detalhes da execução (resultado e total de tentativas efetuadas).
     *
     * @param operacao operação a ser executada
     * @param maxTentativas número máximo de tentativas
     * @param operationName nome da operação para logs
     * @param <T> tipo de retorno
     * @return {@link RetryResult} com o resultado e quantidade de tentativas
     * @throws SpaceApiException se a falha não for 429 ou se as tentativas se esgotarem
     */
    public <T> RetryResult<T> executeWithRetryDetailed(RetryableOperation<T> operacao, int maxTentativas, String operationName) throws SpaceApiException {
        if (maxTentativas < 1) {
            maxTentativas = 1;
        }

        String opName = (operationName != null && !operationName.isBlank()) ? operationName : "Operação";

        for (int attempt = 1; attempt <= maxTentativas; attempt++) {
            try {
                T result = operacao.run();
                return new RetryResult<>(result, attempt);
            } catch (SpaceApiException e) {
                // Retry apenas quando for erro HTTP 429 (Rate Limit)
                if (e.getStatusCode() == 429) {
                    if (attempt < maxTentativas) {
                        long multiplier = 1L << (attempt - 1); // 1, 2, 4, 8...
                        long delayMillis = initialDelay.toMillis() * multiplier;
                        long delaySeconds = delayMillis / 1000;

                        System.out.println(String.format(
                                "[RETRY] %s recebeu 429, aguardando %ds antes da tentativa %d/%d",
                                opName, delaySeconds, attempt + 1, maxTentativas
                        ));

                        try {
                            sleeper.sleep(delayMillis);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new SpaceApiException("Tentativa de retry interrompida: " + ie.getMessage(), ie);
                        }
                        continue;
                    } else {
                        throw new SpaceApiException(429, String.format(
                                "Limite de %d tentativas excedido após rate limit (429) na fonte %s",
                                maxTentativas, opName
                        ), e);
                    }
                }

                // Qualquer outro erro (500, 401, 403, timeout, -1) -> relança imediatamente sem retry
                throw e;
            }
        }

        throw new SpaceApiException(429, "Limite de tentativas excedido após rate limit (429)");
    }
}

