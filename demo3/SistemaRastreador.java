package classesUtilizadas;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class ApiHttpUtil {
    public static String lerResposta(URL url, String userAgent) throws Exception {
        HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
        conexao.setRequestMethod("GET");
        conexao.setConnectTimeout(15000);
        conexao.setReadTimeout(15000);

        if (userAgent != null && !userAgent.isEmpty()) {
            conexao.setRequestProperty("User-Agent", userAgent);
        }

        int codigoResposta = conexao.getResponseCode();
        if (codigoResposta < 200 || codigoResposta >= 300) {
            StringBuilder detalheErro = new StringBuilder();
            try (BufferedReader leitorErro = new BufferedReader(new InputStreamReader(conexao.getErrorStream()))) {
                String linha;
                while ((linha = leitorErro.readLine()) != null) {
                    detalheErro.append(linha);
                }
            } catch (Exception ignored) {
            }

            throw new IllegalStateException("HTTP " + codigoResposta + " ao consultar " + url +
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
    }
}

class BackupDadosObjetos {
    private static final String NOME_ARQUIVO = "dados_backup_objetos.txt";

    private static Path localizarArquivo() {
        Path caminhoDiretorio = Paths.get(System.getProperty("user.dir"));
        Path caminhoArquivo = caminhoDiretorio.resolve(NOME_ARQUIVO);
        if (Files.exists(caminhoArquivo)) {
            return caminhoArquivo;
        }

        Path caminhoAlternativo = caminhoDiretorio.resolve("classesUtilizadas").resolve(NOME_ARQUIVO);
        if (Files.exists(caminhoAlternativo)) {
            return caminhoAlternativo;
        }

        return caminhoArquivo;
    }

    private static List<String> lerLinhas() {
        try {
            Path caminho = localizarArquivo();
            if (!Files.exists(caminho)) {
                return new ArrayList<>();
            }
            return Files.readAllLines(caminho);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Asteroide carregarAsteroide() {
        for (String linha : lerLinhas()) {
            if (linha.trim().isEmpty() || !linha.startsWith("Asteroide|")) {
                continue;
            }
            String[] campos = linha.split("\\|");
            if (campos.length >= 5) {
                try {
                    return new Asteroide(
                            campos[1],
                            new Distancia(Double.parseDouble(campos[2])),
                            Boolean.parseBoolean(campos[3]),
                            LocalDateTime.parse(campos[4], DateTimeFormatter.ISO_DATE_TIME)
                    );
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    public static Satelite carregarSatelite() {
        for (String linha : lerLinhas()) {
            if (linha.trim().isEmpty() || !linha.startsWith("Satelite|")) {
                continue;
            }
            String[] campos = linha.split("\\|");
            if (campos.length >= 5) {
                try {
                    return new Satelite(
                            campos[1],
                            new Distancia(Double.parseDouble(campos[2])),
                            Double.parseDouble(campos[3]),
                            LocalDateTime.parse(campos[4], DateTimeFormatter.ISO_DATE_TIME)
                    );
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    public static Cometa carregarCometa() {
        for (String linha : lerLinhas()) {
            if (linha.trim().isEmpty() || !linha.startsWith("Cometa|")) {
                continue;
            }
            String[] campos = linha.split("\\|");
            if (campos.length >= 5) {
                try {
                    return new Cometa(
                            campos[1],
                            new Distancia(Double.parseDouble(campos[2])),
                            Double.parseDouble(campos[3]),
                            LocalDateTime.parse(campos[4], DateTimeFormatter.ISO_DATE_TIME)
                    );
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }
}

class ClienteNasaNeoWs {

    public static Asteroide buscarPrimeiroAsteroide() {
        try {
            String urlNasa = "https://api.nasa.gov/neo/rest/v1/feed?start_date=2026-08-12&end_date=2026-08-12&api_key=DEMO_KEY";
            URL url = URI.create(urlNasa).toURL();
            String textoJson = ApiHttpUtil.lerResposta(url, null);

            int indexNome = textoJson.indexOf("\"name\":\"") + 8;
            String nome = textoJson.substring(indexNome, textoJson.indexOf("\"", indexNome));

            int indexPerigo = textoJson.indexOf("\"is_potentially_hazardous_asteroid\":") + 36;
            String perigoTexto = textoJson.substring(indexPerigo, indexPerigo + 5);
            boolean perigoso = perigoTexto.startsWith("true");

            int indexDist = textoJson.indexOf("\"kilometers\":\"") + 14;
            String distStr = textoJson.substring(indexDist, textoJson.indexOf("\"", indexDist));
            double distanciaKm = Double.parseDouble(distStr);

            int indexEpoch = textoJson.indexOf("\"epoch_date_close_approach\":") + 29;
            String epochStr = textoJson.substring(indexEpoch, textoJson.indexOf(",", indexEpoch));
            long epochMillis = Long.parseLong(epochStr.trim());
            LocalDateTime ultimoAvistamento = Instant.ofEpochMilli(epochMillis)
                    .atOffset(ZoneOffset.UTC)
                    .toLocalDateTime();

            Distancia distancia = new Distancia(distanciaKm * 1000.0);
            return new Asteroide(nome, distancia, perigoso, ultimoAvistamento);

        } catch (Exception e) {
            System.out.println("Falha ao buscar dados na internet. Usando backup local do projeto.");
            Asteroide backup = BackupDadosObjetos.carregarAsteroide();
            if (backup != null) {
                return backup;
            }
            return null;
        }
    }
}

class TesteApiNasa {
    public static void main(String[] args) {
        try {
            String url = "https://api.nasa.gov/neo/rest/v1/feed?start_date=2026-08-01&api_key=DEMO_KEY";
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Resposta da NASA:\n" + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ClienteCelestrak {

    public static Satelite buscarEstacaoEspacial() {
        try {
            String urlSatelite = "https://celestrak.org/NORAD/elements/gp.php?CATNR=25544&FORMAT=json";
            URL url = URI.create(urlSatelite).toURL();
            String textoJson = ApiHttpUtil.lerResposta(url, "Mozilla/5.0");

            int indexNome = textoJson.indexOf("\"OBJECT_NAME\":\"") + 15;
            String nome = textoJson.substring(indexNome, textoJson.indexOf("\"", indexNome));

            int indexEpoch = textoJson.indexOf("\"EPOCH\":\"") + 10;
            String epochTexto = textoJson.substring(indexEpoch, textoJson.indexOf("\"", indexEpoch));
            LocalDateTime ultimoAvistamento = LocalDateTime.parse(epochTexto, DateTimeFormatter.ISO_DATE_TIME);

            Distancia altitudeAtual = new Distancia(420000.0);
            double decaimentoSimulado = 8.5;

            return new Satelite(nome, altitudeAtual, decaimentoSimulado, ultimoAvistamento);

        } catch (Exception e) {
            System.out.println("Falha ao buscar o Satélite na API. Usando backup local do projeto.");
            Satelite backup = BackupDadosObjetos.carregarSatelite();
            if (backup != null) {
                return backup;
            }
            return null;
        }
    }
}

class ClienteNasaSbdb {

    public static Cometa buscarCometaHalley() {
        try {
            String urlSbdb = "https://ssd-api.jpl.nasa.gov/sbdb.api?sstr=1P";
            URL url = URI.create(urlSbdb).toURL();
            String textoJson = ApiHttpUtil.lerResposta(url, "Mozilla/5.0");

            int indexNome = textoJson.indexOf("\"fullname\":\"") + 12;
            String nome = textoJson.substring(indexNome, textoJson.indexOf("\"", indexNome));

            int indexUltimoObs = textoJson.indexOf("\"last_obs\":\"") + 13;
            String ultimoObsTexto = textoJson.substring(indexUltimoObs, textoJson.indexOf("\"", indexUltimoObs));
            LocalDateTime ultimoAvistamento = LocalDateTime.parse(ultimoObsTexto + "T00:00:00");

            double tamanhoNucleoKm = 11.0;
            Distancia distanciaPerielio = new Distancia(5.0 * 384400000.0);

            return new Cometa(nome, distanciaPerielio, tamanhoNucleoKm, ultimoAvistamento);

        } catch (Exception e) {
            System.out.println("Falha ao buscar o Cometa na API. Usando backup local do projeto.");
            Cometa backup = BackupDadosObjetos.carregarCometa();
            if (backup != null) {
                return backup;
            }
            return null;
        }
    }
}

enum NivelRisco {
    BAIXO, MEDIO, ALTO, CRITICO;
}

class Distancia {
    private double metros; 

    public Distancia(double metros) {
        if (metros < 0) {
            throw new IllegalArgumentException("A distância não pode ser negativa!");
        }
        this.metros = metros;
    }

    public double emKilometros() { return this.metros / 1000.0; }
    public double emMilhas() { return this.metros / 1609.34; }
    public double emDistanciasLunares() { return this.metros / 384400000.0; }
}

class ObjetoEspacial {
    protected String nome;
    protected Distancia distanciaAtual;
    protected String tipo;
    protected LocalDateTime ultimoAvistamento;

    public ObjetoEspacial(String nome, Distancia distanciaAtual, String tipo) {
        this(nome, distanciaAtual, tipo, LocalDateTime.now());
    }

    public ObjetoEspacial(String nome, Distancia distanciaAtual, String tipo, LocalDateTime ultimoAvistamento) {
        this.nome = nome;
        this.distanciaAtual = distanciaAtual;
        this.tipo = tipo;
        this.ultimoAvistamento = ultimoAvistamento;
    }

    public String getNome() { 
        return this.nome; 
    }
    
    public Distancia getDistancia() { 
        return this.distanciaAtual; 
    }
    
    public String getTipo() { 
        return this.tipo; 
    }

    public LocalDateTime getUltimoAvistamento() {
        return this.ultimoAvistamento;
    }
}

class Asteroide extends ObjetoEspacial {
    private boolean ePerigosoPelaNasa;

    public Asteroide(String nome, Distancia distancia, boolean perigoso) {
        this(nome, distancia, perigoso, LocalDateTime.now());
    }

    public Asteroide(String nome, Distancia distancia, boolean perigoso, LocalDateTime ultimoAvistamento) {
        super(nome, distancia, "Asteroide", ultimoAvistamento);
        this.ePerigosoPelaNasa = perigoso;
    }

    public NivelRisco avaliarRisco() {
        if (ePerigosoPelaNasa && distanciaAtual.emDistanciasLunares() < 1.0) {
            return NivelRisco.CRITICO;
        } else if (distanciaAtual.emDistanciasLunares() < 5.0) {
            return NivelRisco.ALTO;
        } else if (distanciaAtual.emDistanciasLunares() < 10.0) {
            return NivelRisco.MEDIO;
        }
        return NivelRisco.BAIXO;
    }
}

class Satelite extends ObjetoEspacial {
    private double decaimentoOrbitalMetrosPorDia;

    public Satelite(String nome, Distancia altitude, double decaimento) {
        this(nome, altitude, decaimento, LocalDateTime.now());
    }

    public Satelite(String nome, Distancia altitude, double decaimento, LocalDateTime ultimoAvistamento) {
        super(nome, altitude, "Satélite", ultimoAvistamento);
        this.decaimentoOrbitalMetrosPorDia = decaimento;
    }

    public NivelRisco avaliarRisco() {
        if (decaimentoOrbitalMetrosPorDia > 1000) { 
            return NivelRisco.CRITICO;
        }
        return NivelRisco.BAIXO;
    }
}

class Cometa extends ObjetoEspacial {
    private double tamanhoNucleoKm;

    public Cometa(String nome, Distancia distancia, double tamanhoNucleo) {
        this(nome, distancia, tamanhoNucleo, LocalDateTime.now());
    }

    public Cometa(String nome, Distancia distancia, double tamanhoNucleo, LocalDateTime ultimoAvistamento) {
        super(nome, distancia, "Cometa", ultimoAvistamento);
        this.tamanhoNucleoKm = tamanhoNucleo;
    }

    public NivelRisco avaliarRisco() {
        if (tamanhoNucleoKm > 10.0 && distanciaAtual.emDistanciasLunares() < 2.0) {
            return NivelRisco.ALTO;
        }
        return NivelRisco.MEDIO;
    }
}

public class SistemaRastreador {

    private static void limparTela() {
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

    private static class ObjetoRastreado {
        private final String nome;
        private final String tipo;
        private final NivelRisco risco;

        public ObjetoRastreado(String nome, String tipo, NivelRisco risco) {
            this.nome = nome;
            this.tipo = tipo;
            this.risco = risco;
        }

        public String getNome() { return nome; }
        public String getTipo() { return tipo; }
        public NivelRisco getRisco() { return risco; }
    }

    private static class PosicaoAtual {
        private final String descricao;
        private final double distanciaAtualKm;
        private final LocalDateTime momentoConsulta;

        public PosicaoAtual(String descricao, double distanciaAtualKm, LocalDateTime momentoReferencia, LocalDateTime momentoConsulta) {
            this.descricao = descricao;
            this.distanciaAtualKm = distanciaAtualKm;
            this.momentoConsulta = momentoConsulta;
        }

        public String getDescricao() { return descricao; }
        public double getDistanciaAtualKm() { return distanciaAtualKm; }
        public LocalDateTime getMomentoConsulta() { return momentoConsulta; }
    }

    private static String formatarDataHora(LocalDateTime dataHora) {
        return dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
    }

    private static String formatarDistancia(double distanciaKm, int opcaoUnidade) {
        if (opcaoUnidade == 1) {
            return String.format("%.2f km", distanciaKm);
        } else if (opcaoUnidade == 2) {
            return String.format("%.2f mi", distanciaKm / 1.60934);
        } else if (opcaoUnidade == 3) {
            return String.format("%.4f LD", distanciaKm / 384400.0);
        }
        return String.format("%.2f km", distanciaKm);
    }

    private static LocalDateTime obterUltimoAvistamento(ObjetoEspacial objeto, LocalDateTime agora) {
        if (objeto.getUltimoAvistamento() != null) {
            return objeto.getUltimoAvistamento();
        }

        long minutosAtras;

        if (objeto instanceof Asteroide) {
            minutosAtras = 30 + Math.round(objeto.getDistancia().emKilometros() / 800000.0);
        } else if (objeto instanceof Satelite) {
            minutosAtras = 20 + Math.round(objeto.getDistancia().emKilometros() / 50000.0);
        } else if (objeto instanceof Cometa) {
            minutosAtras = 60 + Math.round(objeto.getDistancia().emKilometros() / 1000000.0);
        } else {
            minutosAtras = 90;
        }

        return agora.minusMinutes(Math.min(minutosAtras, 720));
    }

    private static double calcularTaxaMovimentoPorMinuto(ObjetoEspacial objeto) {
        double distanciaBaseKm = objeto.getDistancia().emKilometros();

        if (objeto instanceof Asteroide) {
            return distanciaBaseKm * 1.0e-10;
        }
        if (objeto instanceof Satelite) {
            return distanciaBaseKm * 5.0e-10;
        }
        if (objeto instanceof Cometa) {
            return distanciaBaseKm * 1.0e-11;
        }

        return distanciaBaseKm * 1.0e-10;
    }

    private static PosicaoAtual calcularPosicaoAtual(ObjetoEspacial objeto, LocalDateTime referencia, LocalDateTime consulta) {
        double distanciaBaseKm = objeto.getDistancia().emKilometros();
        double minutos = Math.max(0, ChronoUnit.MINUTES.between(referencia, consulta));
        double deslocamentoKm = minutos * calcularTaxaMovimentoPorMinuto(objeto);
        double distanciaAtualKm = Math.max(0.0, distanciaBaseKm - deslocamentoKm);
        String descricao;

        if (objeto instanceof Asteroide) {
            descricao = "órbita heliocêntrica em translação, com deslocamento progressivo em relação ao observador.";
            return new PosicaoAtual(descricao, distanciaAtualKm, referencia, consulta);
        }

        if (objeto instanceof Satelite) {
            descricao = "posição orbital em torno da Terra, com variação de fase por TLE aproximado.";
            return new PosicaoAtual(descricao, distanciaAtualKm, referencia, consulta);
        }

        if (objeto instanceof Cometa) {
            descricao = "trajetória cometária em deslocamento solar, com aproximação gradual ao periélio.";
            return new PosicaoAtual(descricao, distanciaAtualKm, referencia, consulta);
        }

        descricao = "posição dinâmica em relação ao observador, calculada no momento da consulta.";
        return new PosicaoAtual(descricao, distanciaBaseKm, referencia, consulta);
    }

    private static Asteroide criarAsteroideFallback() {
        return new Asteroide("2014 OL339", new Distancia(15000000.0), false);
    }

    private static Satelite criarSateliteFallback() {
        return new Satelite("ISS", new Distancia(400000.0), 45.0);
    }

    private static Cometa criarCometaFallback() {
        return new Cometa("1P/Halley", new Distancia(2.0 * 384400000.0), 11.0);
    }

    private static int lerInteiro(Scanner leitor, String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = leitor.nextLine();
            try {
                return Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite apenas números inteiros.");
            }
        }
    }

    private static int lerQuantidadeObjetos(Scanner leitor, String tipo) {
        int quantidade = lerInteiro(leitor, "Quantos " + tipo + " quer rastrear? ");
        if (quantidade < 0) {
            quantidade = 0;
        }
        return quantidade;
    }

    private static String lerNomeObjeto(Scanner leitor, String tipo) {
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

    private static List<ObjetoRastreado> rastrearObjetos(Scanner leitor, Asteroide asteroide, Satelite satelite, Cometa cometa) {
        List<ObjetoRastreado> objetos = new ArrayList<>();

        System.out.println("\nRastreando Objetos...");

        int qtdAsteroides = lerQuantidadeObjetos(leitor, "asteróides");
        for (int i = 0; i < qtdAsteroides; i++) {
            String nome = lerNomeObjeto(leitor, "Asteroide");
            objetos.add(new ObjetoRastreado(nome, "Asteroide", asteroide.avaliarRisco()));
        }

        int qtdSatelites = lerQuantidadeObjetos(leitor, "satélites");
        for (int i = 0; i < qtdSatelites; i++) {
            String nome = lerNomeObjeto(leitor, "Satélite");
            objetos.add(new ObjetoRastreado(nome, "Satélite", satelite.avaliarRisco()));
        }

        int qtdCometas = lerQuantidadeObjetos(leitor, "cometas");
        for (int i = 0; i < qtdCometas; i++) {
            String nome = lerNomeObjeto(leitor, "Cometa");
            objetos.add(new ObjetoRastreado(nome, "Cometa", cometa.avaliarRisco()));
        }

        objetos.sort(Comparator.comparingInt(obj -> {
            int ordem = 0;
            if (obj.getRisco() == NivelRisco.CRITICO) ordem = 0;
            else if (obj.getRisco() == NivelRisco.ALTO) ordem = 1;
            else if (obj.getRisco() == NivelRisco.MEDIO) ordem = 2;
            else ordem = 3;
            return ordem;
        }));

        return objetos;
    }

    private static void exibirListaRastreada(List<ObjetoRastreado> objetos) {
        System.out.println("\nLista dos objetos em ordem de risco:");
        System.out.printf("%-20s %-25s %s%n", "nome:", "tipo do objeto:", "risco:");
        System.out.println("--------------------------------------------------------------");

        for (ObjetoRastreado objeto : objetos) {
            System.out.printf("%-20s %-25s %s%n",
                    objeto.getNome(),
                    objeto.getTipo(),
                    objeto.getRisco());
        }
    }

    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in);
        boolean executando = true;

        System.out.println("Iniciando o Rastreador Espacial...");
        System.out.println("Conectando aos servidores (NASA NeoWs, SBDB e CelesTrak)... aguarde.\n");

        // Busca de dados prévia
        Asteroide asteroideReal = ClienteNasaNeoWs.buscarPrimeiroAsteroide();
        Satelite lixoEspacial = ClienteCelestrak.buscarEstacaoEspacial();
        Cometa halley = ClienteNasaSbdb.buscarCometaHalley();

        if (asteroideReal == null || lixoEspacial == null || halley == null) {
            System.out.println("Aviso: falha ao obter dados das APIs. Usando objetos de fallback para manter o sistema funcionando.");
            if (asteroideReal == null) asteroideReal = criarAsteroideFallback();
            if (lixoEspacial == null) lixoEspacial = criarSateliteFallback();
            if (halley == null) halley = criarCometaFallback();
        }

        // Loop do Painel 1 (Menu Principal)
        while (executando) {
            System.out.println("\n============== Bem-vindo ao ROVI =============");
            System.out.println("1 - Ver os objetos mais próximos");
            System.out.println("2 - Rastrear objetos");
            System.out.println("3 - Consultar Registros diários");
            System.out.println("4 - Sair do sistema");
            System.out.print("Digite sua escolha (1/2/3/4): ");

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
                    System.out.println("\nSaindo do sistema... Até logo!");
                    executando = false;
                    break;
                default:
                    System.out.println("\nOpção inválida! Voltando ao menu principal.");
            }
        }

        leitor.close();
    }

    // Método responsável pelo Painel 2
    private static void executarPainel2(Scanner leitor, Asteroide asteroide, Satelite satelite, Cometa cometa) {
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
                noPainel2 = false;
                break;
            }
            limparTela();

            if (opcaoPainel2 == 5) {
                noPainel2 = false;
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
                    objetoSelecionado = obterObjetoMaisProximoGlobal(asteroide, satelite, cometa);
                    break;
                default:
                    System.out.println("\nOpção inválida! Voltando ao menu principal.");
                    noPainel2 = false;
                    break;
            }

            if (!noPainel2 && opcaoPainel2 != 5) {
                break;
            }

            if (objetoSelecionado != null) {
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
        // Quantos objetos gostaria de rastreaR?
        // Cometas: 
        // Satelites: 
        //
    }

    private static int perguntarOpcaoAposSaida(Scanner leitor) {
        System.out.println("\nEscolha uma opção:");
        System.out.println("1 - Consultar outro Objeto");
        System.out.println("2 - Voltar ao menu principal");
        System.out.println("3 - Sair da aplicação");
        System.out.print("Digite sua escolha (1/2/3): ");
        return Integer.parseInt(leitor.nextLine().trim());
    }

    // Pergunta o tipo de unidade de medida desejado
    private static int pedirUnidadeDistancia(Scanner leitor) {
        System.out.println("\nEm qual unidade você deseja ver a distância?");
        System.out.println("1 - Quilômetros (km)");
        System.out.println("2 - Milhas (mi)");
        System.out.println("3 - Distâncias Lunares (LD)");
        System.out.print("Digite sua escolha (1/2/3): ");
        return Integer.parseInt(leitor.nextLine().trim());
    }

    private static void executarPainelPosicaoAtual(Scanner leitor, Asteroide asteroide, Satelite satelite, Cometa cometa) {
        System.out.println("\n============== Onde você está agora? ==============");
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
                objetoSelecionado = obterObjetoMaisProximoGlobal(asteroide, satelite, cometa);
                break;
            default:
                System.out.println("\nOpção inválida. Voltando ao menu principal.");
                return;
        }

        if (objetoSelecionado != null) {
            int unidade = pedirUnidadeDistancia(leitor);
            limparTela();
            exibirRelatorio(objetoSelecionado, unidade);

            LocalDateTime agora = LocalDateTime.now();
            LocalDateTime momentoReferencia = obterUltimoAvistamento(objetoSelecionado, agora);
            PosicaoAtual posicaoAtual = calcularPosicaoAtual(objetoSelecionado, momentoReferencia, agora);
            System.out.println("\n============== Onde você está agora? ==============");
            System.out.println("Rastreado em: " + formatarDataHora(momentoReferencia));
            System.out.println("Consulta em: " + formatarDataHora(posicaoAtual.getMomentoConsulta()));
            System.out.println("Distância atual: " + formatarDistancia(posicaoAtual.getDistanciaAtualKm(), unidade));
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

    // Calcula qual objeto está mais próximo dentre todos
    private static ObjetoEspacial obterObjetoMaisProximoGlobal(Asteroide asteroide, Satelite satelite, Cometa cometa) {
        ObjetoEspacial maisProximo = asteroide;

        if (satelite.getDistancia().emKilometros() < maisProximo.getDistancia().emKilometros()) {
            maisProximo = satelite;
        }
        if (cometa.getDistancia().emKilometros() < maisProximo.getDistancia().emKilometros()) {
            maisProximo = cometa;
        }

        return maisProximo;
    }

    // Formata e exibe os dados do objeto selecionado
    private static void exibirRelatorio(ObjetoEspacial obj, int opcaoUnidade) {
        Distancia dist = obj.getDistancia();
        String distanciaFormatada;

        if (opcaoUnidade == 1) {
            distanciaFormatada = formatarDistancia(dist.emKilometros(), 1);
        } else if (opcaoUnidade == 2) {
            distanciaFormatada = formatarDistancia(dist.emKilometros(), 2);
        } else if (opcaoUnidade == 3) {
            distanciaFormatada = formatarDistancia(dist.emKilometros(), 3);
        } else {
            System.out.println("Opção de unidade inválida. Exibindo padrão em km.");
            distanciaFormatada = formatarDistancia(dist.emKilometros(), 1);
        }

        NivelRisco risco = NivelRisco.BAIXO;
        if (obj instanceof Asteroide) {
            risco = ((Asteroide) obj).avaliarRisco();
        } else if (obj instanceof Satelite) {
            risco = ((Satelite) obj).avaliarRisco();
        } else if (obj instanceof Cometa) {
            risco = ((Cometa) obj).avaliarRisco();
        }

        String tipoUpper = obj.getTipo().toUpperCase();

        System.out.println("\n--- RELATÓRIO DO " + tipoUpper + " ESPACIAL ---");
        System.out.println("Nome do Objeto: " + obj.getNome());
        System.out.println("Distância: " + distanciaFormatada);
        System.out.println("Nível de Risco: " + risco);
        System.out.println("-------------------------------------------");
    }
}