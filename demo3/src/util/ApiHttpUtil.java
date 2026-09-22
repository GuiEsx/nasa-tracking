package util;

import exception.ApiException;
import exception.RateLimitExceededException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiHttpUtil {

    private static final int MAX_TENTATIVAS = 3;
    private static final long ESPERA_INICIAL_MS = 2000; // 2 segundos

    private ApiHttpUtil() {
    }

    public static String lerResposta(URL url, String userAgent) throws Exception {
        return lerRespostaComRetry(url, userAgent, MAX_TENTATIVAS, ESPERA_INICIAL_MS);
    }

    public static String lerRespostaComRetry(URL url, String userAgent, int maxTentativas, long esperaInicialMs) throws Exception {
        int tentativas = 0;
        long tempoEspera = esperaInicialMs;
        long tempoTotalAguardado = 0;

        while (tentativas < maxTentativas) {
            tentativas++;
            HttpURLConnection conexao = null;
            try {
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(15000);
                conexao.setReadTimeout(15000);

                if (userAgent != null && !userAgent.isEmpty()) {
                    conexao.setRequestProperty("User-Agent", userAgent);
                }

                int codigoResposta = conexao.getResponseCode();

                // RF8 [X]: Tratamento de Rate Limit (HTTP 429) com retentativa exponencial progressiva
                if (codigoResposta == 429) {
                    if (tentativas < maxTentativas) {
                        long segundosEspera = tempoEspera / 1000;
                        System.out.printf("[RF8 - Rate Limit] A API retornou HTTP 429 (Limite Excedido). Tentativa %d de %d falhou.%n",
                                tentativas, maxTentativas);
                        System.out.printf("[RF8 - Rate Limit] Aguardando %d segundo(s) antes de retentar...%n", segundosEspera);

                        try {
                            Thread.sleep(tempoEspera);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new ApiException("Operação interrompida durante espera de rate limit.", ie);
                        }

                        tempoTotalAguardado += tempoEspera;
                        tempoEspera *= 2; // Espera exponencial: 2s, depois 4s
                        continue;
                    } else {
                        System.out.printf("[RF8 - Desistência Definitiva] Limite de requisições (HTTP 429) persistiu após %d tentativas (total aguardado: %d s). Desistindo da requisição online.%n",
                                tentativas, (tempoTotalAguardado / 1000));
                        throw new RateLimitExceededException(tentativas, tempoTotalAguardado,
                                "Limite de requisições excedido (HTTP 429) após " + tentativas + " tentativas.");
                    }
                }

                if (codigoResposta < 200 || codigoResposta >= 300) {
                    StringBuilder detalheErro = new StringBuilder();
                    try (BufferedReader leitorErro = new BufferedReader(new InputStreamReader(conexao.getErrorStream()))) {
                        String linha;
                        while ((linha = leitorErro.readLine()) != null) {
                            detalheErro.append(linha);
                        }
                    } catch (Exception ignored) {
                    }

                    throw new ApiException(codigoResposta, "HTTP " + codigoResposta + " ao consultar " + url +
                            (detalheErro.length() > 0 ? " - " + detalheErro : ""));
                }

                StringBuilder resposta = new StringBuilder();
                try (BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream()))) {
                    String linha;
                    while ((linha = leitor.readLine()) != null) {
                        resposta.append(linha);
                    }
                }

                return resposta.toString();

            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }

        throw new RateLimitExceededException(tentativas, "Não foi possível concluir a requisição após " + tentativas + " tentativas.");
    }

    /**
     * RF8 [X]: Simulação canônica da especificação:
     * "A NASA retorna HTTP 429 durante uma demonstração em sala com 40 alunos acessando a API.
     * O sistema espera e tenta novamente (ex.: após 2 s, depois 4 s) em vez de travar,
     * e informa o usuário caso desista definitivamente."
     */
    public static void simularCenarioSalaDeAulaRateLimit() throws RateLimitExceededException {
        int maxTentativas = 3;
        long tempoEspera = ESPERA_INICIAL_MS; // 2000 ms = 2s
        long tempoTotalAguardado = 0;

        System.out.println("\n[RF8 - Simulação Canônica] Cenário: Demonstração em sala com 40 alunos consultando a NASA simultaneamente.");
        System.out.println("[RF8 - Simulação Canônica] Conectando ao endpoint da API da NASA...\n");

        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            if (tentativa < maxTentativas) {
                long segundos = tempoEspera / 1000;
                System.out.printf("[RF8 - Rate Limit] A API da NASA retornou HTTP 429 (Too Many Requests). Tentativa %d de %d.%n",
                        tentativa, maxTentativas);
                System.out.printf("[RF8 - Rate Limit] Sistema aguardando %d segundo(s) antes de tentar novamente...%n%n", segundos);

                try {
                    Thread.sleep(tempoEspera);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ApiException("Simulação interrompida.", ie);
                }

                tempoTotalAguardado += tempoEspera;
                tempoEspera *= 2; // 2s na tentativa 1, 4s na tentativa 2
            } else {
                System.out.printf("[RF8 - Rate Limit] A API da NASA retornou HTTP 429 na tentativa %d de %d (Tentativa final).%n",
                        tentativa, maxTentativas);
                System.out.printf("[RF8 - Desistência Definitiva] Limite de requisições persistiu após %d tentativas (tempo total de espera: %d s).%n",
                        tentativa, (tempoTotalAguardado / 1000));
                System.out.println("[RF8 - Desistência Definitiva] O sistema encerra as tentativas para evitar travamento da aplicação.\n");
                throw new RateLimitExceededException(tentativa, tempoTotalAguardado,
                        "Limite de requisições excedido (HTTP 429 da NASA após 40 acessos simultâneos).");
            }
        }
    }
}
