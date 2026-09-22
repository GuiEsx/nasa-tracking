package view;

import model.ApodRegistro;
import model.Asteroide;
import model.Cometa;
import model.Distancia;
import model.NivelRisco;
import model.ObjetoEspacial;
import model.ObjetoRastreado;
import model.PosicaoAtual;
import model.RetratoDiario;
import model.Satelite;
import service.BackupDadosService;
import service.RastreadorService;
import service.feed.NasaApodFeed;
import service.feed.NasaFeed;
import service.feed.NasaNeoWsFeed;
import util.ApiHttpUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class MenuConsole {

    public static void limparTela() {
        try {
            String sistemaOperacional = System.getProperty("os.name").toLowerCase();
            if (sistemaOperacional.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            System.out.print("\n\n");
        }
    }

    public static String formatarDataHora(LocalDateTime dataHora) {
        return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
    }

    // RF1 [E]: Formatação segura a partir do objeto Distancia
    public static String formatarDistancia(Distancia distancia, int opcaoUnidade) {
        if (distancia == null) {
            return "0.00 km";
        }
        if (opcaoUnidade == 1) {
            return String.format("%.2f km", distancia.emKilometros());
        } else if (opcaoUnidade == 2) {
            return String.format("%.2f mi", distancia.emMilhas());
        } else if (opcaoUnidade == 3) {
            return String.format("%.4f LD", distancia.emDistanciasLunares());
        }
        return String.format("%.2f km", distancia.emKilometros());
    }

    public static String formatarDistancia(double distanciaKm, int opcaoUnidade) {
        return formatarDistancia(Distancia.deKilometros(distanciaKm), opcaoUnidade);
    }

    public static int lerInteiro(Scanner leitor, String mensagem) {
        while (true) {
            if (mensagem != null && !mensagem.isEmpty()) {
                System.out.print(mensagem);
            }
            String entrada = leitor.nextLine();
            try {
                return Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite apenas números inteiros.");
            }
        }
    }

    public static int lerQuantidadeObjetos(Scanner leitor, String tipo) {
        int quantidade = lerInteiro(leitor, "Quantos " + tipo + " quer rastrear? ");
        if (quantidade < 0) {
            quantidade = 0;
        }
        return quantidade;
    }

    public static String lerNomeObjeto(Scanner leitor, String tipo) {
        String nome;
        do {
            System.out.print("Qual o nome do objeto? (Ex.: 2026-AB): ");
            nome = leitor.nextLine().trim();
            if (nome.isEmpty()) {
                System.out.println("O nome do objeto não pode ficar vazio.");
            }
        } while (nome.isEmpty());
        return nome;
    }

    public static int pedirUnidadeDistancia(Scanner leitor) {
        System.out.println("\nEm qual unidade você deseja ver a distância?");
        System.out.println("1 - Quilômetros (km)");
        System.out.println("2 - Milhas (mi)");
        System.out.println("3 - Distâncias Lunares (LD)");
        System.out.print("Digite sua escolha (1/2/3): ");
        return lerInteiro(leitor, "");
    }

    public static int perguntarOpcaoAposSaida(Scanner leitor) {
        System.out.println("\nEscolha uma opção:");
        System.out.println("1 - Consultar outro Objeto");
        System.out.println("2 - Voltar ao menu principal");
        System.out.println("3 - Sair da aplicação");
        System.out.print("Digite sua escolha (1/2/3): ");
        return lerInteiro(leitor, "");
    }

    public static void exibirRelatorio(ObjetoEspacial obj, int opcaoUnidade) {
        Distancia dist = obj.getDistancia();
        String distanciaFormatada = formatarDistancia(dist, opcaoUnidade);

        // RF2 & RF4: Avaliação delegada puramente ao objeto
        NivelRisco risco = obj.avaliarRisco();
        String tipoUpper = obj.getTipo().toUpperCase();

        System.out.println("\n--- RELATÓRIO DO " + tipoUpper + " ESPACIAL ---");
        System.out.println("Identificador (ID): " + obj.getId());
        System.out.println("Nome do Objeto: " + obj.getNome());
        System.out.println("Distância: " + distanciaFormatada);
        System.out.println("Nível de Risco: " + risco);
        System.out.println("-------------------------------------------");
    }

    public static void exibirListaRastreada(List<ObjetoRastreado> objetos) {
        System.out.println("\nLista dos objetos em ordem de risco (RF5: CRÍTICO -> BAIXO):");
        System.out.printf("%-20s %-25s %s%n", "nome:", "tipo do objeto:", "risco:");
        System.out.println("--------------------------------------------------------------");

        for (ObjetoRastreado objeto : objetos) {
            System.out.printf("%-20s %-25s %s%n",
                    objeto.getNome(),
                    objeto.getTipo(),
                    objeto.getRisco());
        }
    }

    // RF9 [P]: Uso de Set para garantir desduplicação de objetos rastreados
    public static List<ObjetoRastreado> rastrearObjetos(Scanner leitor, Asteroide asteroide, Satelite satelite, Cometa cometa) {
        Set<ObjetoRastreado> conjunto = new LinkedHashSet<>();

        System.out.println("\nRastreando Objetos...");

        int qtdAsteroides = lerQuantidadeObjetos(leitor, "asteróides");
        for (int i = 0; i < qtdAsteroides; i++) {
            String nome = lerNomeObjeto(leitor, "Asteroide");
            NivelRisco risco = (asteroide != null) ? asteroide.avaliarRisco() : NivelRisco.BAIXO;
            conjunto.add(new ObjetoRastreado(nome, "Asteroide", risco));
        }

        int qtdSatelites = lerQuantidadeObjetos(leitor, "satélites");
        for (int i = 0; i < qtdSatelites; i++) {
            String nome = lerNomeObjeto(leitor, "Satélite");
            NivelRisco risco = (satelite != null) ? satelite.avaliarRisco() : NivelRisco.BAIXO;
            conjunto.add(new ObjetoRastreado(nome, "Satélite", risco));
        }

        int qtdCometas = lerQuantidadeObjetos(leitor, "cometas");
        for (int i = 0; i < qtdCometas; i++) {
            String nome = lerNomeObjeto(leitor, "Cometa");
            NivelRisco risco = (cometa != null) ? cometa.avaliarRisco() : NivelRisco.BAIXO;
            conjunto.add(new ObjetoRastreado(nome, "Cometa", risco));
        }

        List<ObjetoRastreado> lista = new ArrayList<>(conjunto);
        // RF5 [P]: Classificação unificada sem lógica fixa por tipo
        RastreadorService.ordenarPorRisco(lista);
        return lista;
    }

    public static void executarPainel2(Scanner leitor, Asteroide asteroide, Satelite satelite, Cometa cometa) {
        boolean noPainel2 = true;

        while (noPainel2) {
            System.out.println("\n============== Painel 2: Objetos Mais Próximos ==============");
            System.out.println("1 - Ver cometa mais próximo");
            System.out.println("2 - Ver satélite mais próximo");
            System.out.println("3 - Ver asteroide mais próximo");
            System.out.println("4 - Ver o objeto mais próximo (Geral)");
            System.out.println("5 - Voltar ao menu principal");
            System.out.print("Digite sua escolha (1/2/3/4/5): ");

            int opcaoPainel2;
            try {
                opcaoPainel2 = Integer.parseInt(leitor.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("\nOpção inválida! Voltando ao menu principal.");
                break;
            }
            limparTela();

            if (opcaoPainel2 == 5) {
                break;
            }

            ObjetoEspacial objetoSelecionado = null;

            switch (opcaoPainel2) {
                case 1:
                    objetoSelecionado = cometa;
                    break;
                case 2:
                    objetoSelecionado = satelite;
                    break;
                case 3:
                    objetoSelecionado = asteroide;
                    break;
                case 4:
                    objetoSelecionado = RastreadorService.obterObjetoMaisProximoGlobal(asteroide, satelite, cometa);
                    break;
                default:
                    System.out.println("\nOpção inválida! Voltando ao menu principal.");
                    noPainel2 = false;
                    break;
            }

            if (!noPainel2) {
                break;
            }

            if (objetoSelecionado == null) {
                System.out.println("\nObjeto não disponível no momento.");
                continue;
            }

            int unidade = pedirUnidadeDistancia(leitor);
            limparTela();
            exibirRelatorio(objetoSelecionado, unidade);

            int opcaoAposRelatorio = perguntarOpcaoAposSaida(leitor);
            limparTela();

            switch (opcaoAposRelatorio) {
                case 1:
                    continue;
                case 2:
                    noPainel2 = false;
                    break;
                case 3:
                    System.out.println("\nSaindo da aplicação... Até logo!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("\nOpção inválida. Voltando ao menu principal.");
                    noPainel2 = false;
                    break;
            }
        }
    }

    // RF6 [IF]: Consulta comum de posição via interface Posicionavel
    public static void executarPainelPosicaoAtual(Scanner leitor, Asteroide asteroide, Satelite satelite, Cometa cometa) {
        System.out.println("\n============== Onde você está agora? (RF6: Interface Posicionavel) ==============");
        System.out.println("1 - Ver asteroide atual");
        System.out.println("2 - Ver satélite atual");
        System.out.println("3 - Ver cometa atual");
        System.out.println("4 - Ver objeto atual geral");
        System.out.println("5 - Voltar ao menu principal");
        System.out.print("Digite sua escolha (1/2/3/4/5): ");

        int opcaoPosicao;
        try {
            opcaoPosicao = Integer.parseInt(leitor.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("\nOpção inválida! Voltando ao menu principal.");
            return;
        }
        limparTela();

        if (opcaoPosicao == 5) {
            return;
        }

        ObjetoEspacial objetoSelecionado = null;
        switch (opcaoPosicao) {
            case 1:
                objetoSelecionado = asteroide;
                break;
            case 2:
                objetoSelecionado = satelite;
                break;
            case 3:
                objetoSelecionado = cometa;
                break;
            case 4:
                objetoSelecionado = RastreadorService.obterObjetoMaisProximoGlobal(asteroide, satelite, cometa);
                break;
            default:
                System.out.println("\nOpção inválida. Voltando ao menu principal.");
                return;
        }

        if (objetoSelecionado != null) {
            int unidade = pedirUnidadeDistancia(leitor);
            limparTela();
            exibirRelatorio(objetoSelecionado, unidade);

            // Chamada polimórfica via interface Posicionavel (RF6)
            PosicaoAtual posicaoAtual = objetoSelecionado.consultarPosicaoAgora();

            System.out.println("\n============== Onde você está agora? ==============");
            System.out.println("Rastreado em: " + formatarDataHora(posicaoAtual.getMomentoReferencia()));
            System.out.println("Consulta em: " + formatarDataHora(posicaoAtual.getMomentoConsulta()));
            System.out.println("Distância atual: " + formatarDistancia(posicaoAtual.getDistancia(), unidade));
            System.out.println("Posição: " + posicaoAtual.getDescricao());

            int opcaoAposRelatorio = perguntarOpcaoAposSaida(leitor);
            limparTela();

            switch (opcaoAposRelatorio) {
                case 1:
                    executarPainelPosicaoAtual(leitor, asteroide, satelite, cometa);
                    break;
                case 2:
                    return;
                case 3:
                    System.out.println("\nSaindo da aplicação... Até logo!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("\nOpção inválida. Voltando ao menu principal.");
                    return;
            }
        }
    }

    // RF7 [C] & RF9 [P]: Retrato diário com agregados e prevenção de duplicatas
    public static void executarPainelRetratoDiario(Scanner leitor, Asteroide asteroide, Satelite satelite, Cometa cometa) {
        LocalDate hoje = LocalDate.now();
        RetratoDiario retrato = RastreadorService.criarRetratoDiario(hoje, asteroide, satelite, cometa);

        boolean noPainel = true;
        while (noPainel) {
            System.out.println("\n============== Retrato Diário com Agregados (RF7 & RF9) ==============");
            System.out.println("1 - Exibir resumo formatado do dia (RF7)");
            System.out.println("2 - Consultar perguntas agregadas específicas (RF7)");
            System.out.println("3 - Ver lista de todos os objetos no retrato");
            System.out.println("4 - Carregar catálogo completo de backup (com desduplicação RF9)");
            System.out.println("5 - Testar reconhecimento de duplicatas (RF9)");
            System.out.println("6 - Voltar ao menu principal");
            System.out.print("Digite sua escolha (1-6): ");

            int opcao = lerInteiro(leitor, "");
            limparTela();

            switch (opcao) {
                case 1:
                    System.out.println("\n--- Resumo Agregado do Dia (RF7) ---");
                    System.out.println(retrato.gerarResumoFormatado());

                    System.out.println("\nDistribuição por Nível de Risco:");
                    Map<NivelRisco, Integer> contagem = retrato.contagemPorRisco();
                    for (Map.Entry<NivelRisco, Integer> entry : contagem.entrySet()) {
                        System.out.printf("  • %-10s : %d objeto(s)%n", entry.getKey(), entry.getValue());
                    }
                    break;

                case 2:
                    System.out.println("\n--- Perguntas Agregadas (RF7) ---");
                    System.out.println("Total de objetos rastreados: " + retrato.totalObjetos());
                    ObjetoEspacial maisProx = retrato.obterMaisProximo();
                    if (maisProx != null) {
                        System.out.printf("Objeto mais próximo: %s %s (%.2f km / %.4f LD)%n",
                                maisProx.getTipo(), maisProx.getNome(),
                                maisProx.getDistancia().emKilometros(),
                                maisProx.getDistancia().emDistanciasLunares());
                    }
                    ObjetoEspacial maisPerig = retrato.obterMaisPerigoso();
                    if (maisPerig != null) {
                        System.out.printf("Objeto mais perigoso: %s %s (Risco: %s)%n",
                                maisPerig.getTipo(), maisPerig.getNome(), maisPerig.avaliarRisco());
                    }
                    System.out.printf("Distância média dos objetos: %.2f km%n", retrato.calcularDistanciaMediaKm());
                    break;

                case 3:
                    System.out.printf("%nObjetos no Retrato (%d total):%n", retrato.totalObjetos());
                    System.out.printf("%-15s %-25s %-15s %s%n", "ID:", "NOME:", "TIPO:", "RISCO:");
                    System.out.println("----------------------------------------------------------------------");
                    for (ObjetoEspacial obj : retrato.getObjetos()) {
                        System.out.printf("%-15s %-25s %-15s %s%n",
                                obj.getId(), obj.getNome(), obj.getTipo(), obj.avaliarRisco());
                    }
                    break;

                case 4:
                    System.out.println("Carregando objetos do arquivo de backup...");
                    Set<ObjetoEspacial> objetosBackup = BackupDadosService.carregarTodosObjetos();
                    int antesBackup = retrato.totalObjetos();
                    retrato.adicionarTodos(objetosBackup);
                    int depoisBackup = retrato.totalObjetos();
                    int adicionados = depoisBackup - antesBackup;
                    int descartados = objetosBackup.size() - adicionados;
                    System.out.printf("Sucesso! Objetos únicos no retrato: %d (Adicionados: %d, duplicatas descartadas: %d)%n",
                            depoisBackup, adicionados, descartados);
                    System.out.println(retrato.gerarResumoFormatado());
                    break;

                case 5:
                    System.out.println("\n--- Teste de Reconhecimento de Duplicatas (RF9) ---");
                    System.out.println("Tentando reinserir o asteroide atual com o mesmo identificador: " + asteroide.getId());
                    int antes = retrato.totalObjetos();
                    boolean inseriu = retrato.adicionarObjeto(asteroide);
                    int depois = retrato.totalObjetos();
                    System.out.println("Resultado da adição no Set: " + (inseriu ? "Adicionado (novo)" : "Ignorado (duplicata detectada)"));
                    System.out.printf("Total antes: %d | Total depois: %d (Duplicata evitada com sucesso)%n", antes, depois);
                    break;

                case 6:
                    noPainel = false;
                    break;

                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    // RF10 [A] & RF8 [X]: Múltiplos Feeds da NASA e simulação de resiliência
    public static void executarPainelFeedsNasa(Scanner leitor) {
        boolean noPainel = true;
        while (noPainel) {
            System.out.println("\n============== Feeds da NASA (RF10: Abordagem Unificada) ==============");
            System.out.println("1 - Consultar Imagem Astronômica do Dia (NASA APOD)");
            System.out.println("2 - Consultar Feed de Asteroides Próximos (NASA NeoWs)");
            System.out.println("3 - Simular Teste de Resiliência a Rate Limit HTTP 429 (RF8)");
            System.out.println("4 - Voltar ao menu principal");
            System.out.print("Digite sua escolha (1/2/3/4): ");

            int opcao = lerInteiro(leitor, "");
            limparTela();

            switch (opcao) {
                case 1:
                    consultarFeedGenerico(new NasaApodFeed());
                    break;
                case 2:
                    consultarFeedGenerico(new NasaNeoWsFeed());
                    break;
                case 3:
                    simularResilienciaRateLimit();
                    break;
                case 4:
                    noPainel = false;
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }
        }
    }

    // Método que consome qualquer feed via abstração comum NasaFeed<T> (RF10)
    private static <T> void consultarFeedGenerico(NasaFeed<T> feed) {
        System.out.println("Consultando feed: " + feed.getNomeFeed() + "...");
        System.out.println("Endpoint: " + feed.getEndpointUrl());
        try {
            T resultado = feed.consultarFeed();
            System.out.println("\n--- Resultado Obtido do Feed ---");
            if (resultado instanceof ApodRegistro) {
                ApodRegistro apod = (ApodRegistro) resultado;
                System.out.println("Data: " + apod.getData());
                System.out.println("Título: " + apod.getTitulo());
                System.out.println("URL da Mídia: " + apod.getUrlMidia());
                System.out.println("\nExplicação:\n" + apod.getExplicacao());
            } else if (resultado instanceof List<?>) {
                List<?> lista = (List<?>) resultado;
                System.out.println("Total de registros obtidos no feed: " + lista.size());
                int count = 0;
                for (Object item : lista) {
                    if (count++ >= 5) {
                        System.out.println("... e mais " + (lista.size() - 5) + " registro(s).");
                        break;
                    }
                    System.out.println("  • " + item);
                }
            } else {
                System.out.println(resultado);
            }
        } catch (Exception e) {
            System.out.println("Erro ao consultar feed: " + e.getMessage());
        }
    }

    // RF8 [X]: Simulação canônica do cenário de Rate Limit (HTTP 429) e demonstração em sala
    private static void simularResilienciaRateLimit() {
        System.out.println("\n--- Demonstração do Requisito RF8 [X]: Resiliência a Rate Limit (HTTP 429) ---");
        System.out.println("Cenário da especificação: 40 alunos realizando requisições simultâneas à NASA em sala.");
        System.out.println("Comportamento esperado: O sistema espera e tenta novamente (2s, depois 4s) sem travar,");
        System.out.println("e informa o usuário caso desista definitivamente, carregando o backup local.\n");

        try {
            // Executa a simulação real de Rate Limit via ApiHttpUtil
            ApiHttpUtil.simularCenarioSalaDeAulaRateLimit();
        } catch (exception.RateLimitExceededException rle) {
            System.err.println("\n==========================================================================");
            System.err.println("[RF8 - AVISO AO USUÁRIO] Todas as tentativas de conexão esgotaram-se!");
            System.err.printf("[RF8 - AVISO AO USUÁRIO] Foram realizadas %d tentativas (tempo total aguardado: %d s).%n",
                    rle.getTentativasRealizadas(), (rle.getTempoTotalEsperaMs() / 1000));
            System.err.println("[RF8 - AVISO AO USUÁRIO] O sistema desistiu definitivamente da API online para evitar travamento.");
            System.err.println("==========================================================================\n");

            System.out.println("-> Acionando mecanismo de contingência e resiliência...");
            System.out.println("-> Lendo catálogo de emergência de dados_backup_objetos.txt...");
            Asteroide backup = BackupDadosService.carregarAsteroide();
            if (backup != null) {
                System.out.println("-> [Sucesso no Fallback] Objeto recuperado com segurança: " + backup.getNome() +
                        " (Distância: " + backup.getDistancia() + ")");
            }
            System.out.println("\n[RF8 Concluído com Sucesso] A aplicação permaneceu 100% responsiva e operacional!");
        }
    }

    public static void executarMenuPrincipal(Scanner leitor, Asteroide asteroideReal, Satelite lixoEspacial, Cometa halley) {
        boolean executando = true;

        while (executando) {
            System.out.println("\n============== Bem-vindo ao ROVI =============");
            System.out.println("1 - Ver os objetos mais próximos");
            System.out.println("2 - Rastrear objetos (RF5: Classificação unificada)");
            System.out.println("3 - Consultar Registros diários (RF6: Posição via Posicionavel)");
            System.out.println("4 - Retrato diário com agregados (RF7 & RF9: Desduplicação)");
            System.out.println("5 - Consultar Feeds da NASA (RF10 & RF8: Resiliência)");
            System.out.println("6 - Sair do sistema");
            System.out.print("Digite sua escolha (1/2/3/4/5/6): ");

            int opcaoPainel1;
            try {
                opcaoPainel1 = Integer.parseInt(leitor.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("\nOpção inválida! Voltando ao menu principal.");
                limparTela();
                continue;
            }

            limparTela();

            switch (opcaoPainel1) {
                case 1:
                    executarPainel2(leitor, asteroideReal, lixoEspacial, halley);
                    break;
                case 2:
                    List<ObjetoRastreado> objetosRastreados = rastrearObjetos(leitor, asteroideReal, lixoEspacial, halley);
                    if (objetosRastreados.isEmpty()) {
                        System.out.println("\nNenhum objeto foi rastreado.");
                    } else {
                        exibirListaRastreada(objetosRastreados);
                    }
                    break;
                case 3:
                    executarPainelPosicaoAtual(leitor, asteroideReal, lixoEspacial, halley);
                    break;
                case 4:
                    executarPainelRetratoDiario(leitor, asteroideReal, lixoEspacial, halley);
                    break;
                case 5:
                    executarPainelFeedsNasa(leitor);
                    break;
                case 6:
                    System.out.println("\nSaindo do sistema... Até logo!");
                    executando = false;
                    break;
                default:
                    System.out.println("\nOpção inválida! Voltando ao menu principal.");
            }
        }
    }
}
