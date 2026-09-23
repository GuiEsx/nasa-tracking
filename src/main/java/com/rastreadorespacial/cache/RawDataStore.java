package com.rastreadorespacial.cache;

import com.rastreadorespacial.config.AppConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utilitário responsável por persistir e recuperar respostas brutas das APIs no sistema de arquivos local.
 */
public class RawDataStore {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private final Path baseDataDir;

    public RawDataStore() {
        this(AppConfig.DATA_DIR);
    }

    public RawDataStore(Path baseDataDir) {
        this.baseDataDir = baseDataDir;
    }

    /**
     * Salva a resposta JSON bruta em disco no caminho {@code {baseDataDir}/{source}/{yyyy-MM-dd}.json}.
     * Se já existir um arquivo para a data atual, o conteúdo é sobrescrito.
     *
     * @param source nome da fonte ("neows", "apod" ou "celestrak")
     * @param rawJson conteúdo bruto em formato JSON
     */
    public void save(String source, String rawJson) {
        saveAndGetPath(source, rawJson);
    }

    /**
     * Salva a resposta JSON bruta em disco e retorna o caminho do arquivo criado.
     *
     * @param source nome da fonte ("neows", "apod" ou "celestrak")
     * @param rawJson conteúdo bruto em formato JSON
     * @return o caminho (Path) do arquivo salvo
     */
    public Path saveAndGetPath(String source, String rawJson) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("A fonte (source) não pode ser nula ou vazia.");
        }
        if (rawJson == null) {
            throw new IllegalArgumentException("O conteúdo JSON não pode ser nulo.");
        }

        String fileName = LocalDate.now().format(DATE_FORMATTER) + ".json";
        Path targetDir = baseDataDir.resolve(source.toLowerCase());
        Path targetFile = targetDir.resolve(fileName);

        try {
            Files.createDirectories(targetDir);
            Files.writeString(
                    targetFile,
                    rawJson,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            return targetFile;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar dados brutos em " + targetFile, e);
        }
    }

    /**
     * Carrega o arquivo de cache mais recente disponível para uma determinada fonte.
     *
     * @param source nome da fonte ("neows", "apod" ou "celestrak")
     * @return Optional contendo o CachedDataResult se houver cache disponível, ou Optional.empty()
     */
    public Optional<CachedDataResult> loadMostRecent(String source) {
        if (source == null || source.isBlank()) {
            return Optional.empty();
        }

        Path sourceDir = baseDataDir.resolve(source.toLowerCase());
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            return Optional.empty();
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir, "*.json")) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        String fileName = path.getFileName().toString();
                        if (fileName.endsWith(".json") && fileName.length() >= 15) {
                            try {
                                String datePart = fileName.substring(0, 10);
                                LocalDate date = LocalDate.parse(datePart, DATE_FORMATTER);
                                return new FileDateEntry(path, date);
                            } catch (DateTimeParseException ignored) {
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(entry -> entry != null)
                    .max(Comparator.comparing(FileDateEntry::date))
                    .map(entry -> {
                        try {
                            String rawJson = Files.readString(entry.path(), StandardCharsets.UTF_8);
                            return new CachedDataResult(source, rawJson, true, entry.date());
                        } catch (IOException e) {
                            throw new UncheckedIOException("Falha ao ler arquivo de cache: " + entry.path(), e);
                        }
                    });
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Retorna o caminho do arquivo para uma determinada fonte e data.
     *
     * @param source nome da fonte
     * @param date data da consulta
     * @return Path correspondente
     */
    public Path resolvePath(String source, LocalDate date) {
        String fileName = date.format(DATE_FORMATTER) + ".json";
        return baseDataDir.resolve(source.toLowerCase()).resolve(fileName);
    }

    private record FileDateEntry(Path path, LocalDate date) {}
}

