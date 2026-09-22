package service;

import model.Asteroide;
import model.Cometa;
import model.Distancia;
import model.ObjetoEspacial;
import model.RetratoDiario;
import model.Satelite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BackupDadosService {
    private static final String NOME_ARQUIVO = "dados_backup_objetos.txt";

    private BackupDadosService() {
    }

    private static Path localizarArquivo() {
        Path caminhoDiretorio = Paths.get(System.getProperty("user.dir"));

        Path[] possiveisCaminhos = new Path[]{
                caminhoDiretorio.resolve(NOME_ARQUIVO),
                caminhoDiretorio.resolve("demo3").resolve(NOME_ARQUIVO),
                caminhoDiretorio.resolve("resources").resolve(NOME_ARQUIVO),
                caminhoDiretorio.resolve("demo3").resolve("resources").resolve(NOME_ARQUIVO),
                caminhoDiretorio.resolve("..").resolve(NOME_ARQUIVO),
                caminhoDiretorio.resolve("..").resolve("resources").resolve(NOME_ARQUIVO),
                caminhoDiretorio.resolve("..").resolve("demo3").resolve(NOME_ARQUIVO),
                caminhoDiretorio.resolve("classesUtilizadas").resolve(NOME_ARQUIVO)
        };

        for (Path caminho : possiveisCaminhos) {
            if (Files.exists(caminho)) {
                return caminho;
            }
        }

        return caminhoDiretorio.resolve(NOME_ARQUIVO);
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
        List<Asteroide> todos = carregarTodosAsteroides();
        return !todos.isEmpty() ? todos.get(0) : null;
    }

    public static List<Asteroide> carregarTodosAsteroides() {
        List<Asteroide> lista = new ArrayList<>();
        for (String linha : lerLinhas()) {
            if (linha.trim().isEmpty() || !linha.startsWith("Asteroide|")) {
                continue;
            }
            String[] campos = linha.split("\\|");
            if (campos.length >= 5) {
                try {
                    String nome = campos[1].trim();
                    lista.add(new Asteroide(
                            nome,
                            nome,
                            Distancia.deMetros(Double.parseDouble(campos[2].trim())),
                            Boolean.parseBoolean(campos[3].trim()),
                            LocalDateTime.parse(campos[4].trim(), DateTimeFormatter.ISO_DATE_TIME)
                    ));
                } catch (Exception ignored) {
                }
            }
        }
        return lista;
    }

    public static Satelite carregarSatelite() {
        List<Satelite> todos = carregarTodosSatelites();
        return !todos.isEmpty() ? todos.get(0) : null;
    }

    public static List<Satelite> carregarTodosSatelites() {
        List<Satelite> lista = new ArrayList<>();
        for (String linha : lerLinhas()) {
            if (linha.trim().isEmpty() || !linha.startsWith("Satelite|")) {
                continue;
            }
            String[] campos = linha.split("\\|");
            if (campos.length >= 5) {
                try {
                    String nome = campos[1].trim();
                    lista.add(new Satelite(
                            nome,
                            nome,
                            Distancia.deMetros(Double.parseDouble(campos[2].trim())),
                            Double.parseDouble(campos[3].trim()),
                            LocalDateTime.parse(campos[4].trim(), DateTimeFormatter.ISO_DATE_TIME)
                    ));
                } catch (Exception ignored) {
                }
            }
        }
        return lista;
    }

    public static Cometa carregarCometa() {
        List<Cometa> todos = carregarTodosCometas();
        return !todos.isEmpty() ? todos.get(0) : null;
    }

    public static List<Cometa> carregarTodosCometas() {
        List<Cometa> lista = new ArrayList<>();
        for (String linha : lerLinhas()) {
            if (linha.trim().isEmpty() || !linha.startsWith("Cometa|")) {
                continue;
            }
            String[] campos = linha.split("\\|");
            if (campos.length >= 5) {
                try {
                    String nome = campos[1].trim();
                    lista.add(new Cometa(
                            nome,
                            nome,
                            Distancia.deMetros(Double.parseDouble(campos[2].trim())),
                            Double.parseDouble(campos[3].trim()),
                            LocalDateTime.parse(campos[4].trim(), DateTimeFormatter.ISO_DATE_TIME)
                    ));
                } catch (Exception ignored) {
                }
            }
        }
        return lista;
    }

    public static Set<ObjetoEspacial> carregarTodosObjetos() {
        Set<ObjetoEspacial> conjunto = new LinkedHashSet<>();
        conjunto.addAll(carregarTodosAsteroides());
        conjunto.addAll(carregarTodosSatelites());
        conjunto.addAll(carregarTodosCometas());
        return conjunto;
    }

    public static RetratoDiario criarRetratoDiarioDeBackup(LocalDate data) {
        RetratoDiario retrato = new RetratoDiario(data != null ? data : LocalDate.now());
        retrato.adicionarTodos(carregarTodosObjetos());
        return retrato;
    }
}
