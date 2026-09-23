package com.rastreadorespacial.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rastreadorespacial.domain.DailySnapshot;
import com.rastreadorespacial.domain.SnapshotFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Persiste o resumo mínimo de cada retrato para permitir consulta histórica. */
final class SnapshotHistoryStore {
    private final Path directory;
    private final ObjectMapper mapper = new ObjectMapper();

    SnapshotHistoryStore(Path directory) {
        this.directory = directory;
    }

    void salvar(DailySnapshot snapshot) {
        try {
            Files.createDirectories(directory);
            ObjectNode json = mapper.createObjectNode();
            json.put("data", snapshot.getData().toString());
            json.put("quantidadeObjetosUnicos", snapshot.quantidadeDeObjetos());
            json.put("resumo", SnapshotFormatter.resumoTextual(snapshot));
            mapper.writerWithDefaultPrettyPrinter().writeValue(arquivo(snapshot.getData()).toFile(), json);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar o retrato diário.", e);
        }
    }

    List<LocalDate> listarDatas() {
        if (!Files.isDirectory(directory)) return List.of();
        try (var arquivos = Files.list(directory)) {
            return arquivos.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(this::dataDoArquivo)
                    .filter(java.util.Objects::nonNull)
                    .sorted(Comparator.reverseOrder())
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private Path arquivo(LocalDate data) {
        return directory.resolve(data + ".json");
    }

    private LocalDate dataDoArquivo(Path path) {
        try {
            return LocalDate.parse(path.getFileName().toString().replace(".json", ""));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
