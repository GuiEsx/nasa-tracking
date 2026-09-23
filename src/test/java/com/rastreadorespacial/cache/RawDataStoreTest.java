package com.rastreadorespacial.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RawDataStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void testSaveCreatesFileWithCorrectContent() throws IOException {
        RawDataStore store = new RawDataStore(tempDir);
        String sampleJson = "{\"title\": \"Space Object\", \"active\": true}";
        String source = "neows";

        Path savedPath = store.saveAndGetPath(source, sampleJson);

        assertTrue(Files.exists(savedPath), "O arquivo deve ser criado no disco");

        String expectedFileName = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".json";
        assertEquals(expectedFileName, savedPath.getFileName().toString());
        assertEquals(source, savedPath.getParent().getFileName().toString());

        String content = Files.readString(savedPath, StandardCharsets.UTF_8);
        assertEquals(sampleJson, content, "O conteúdo salvo deve ser exatamente igual ao JSON original");
    }

    @Test
    void testSaveOverwritesExistingFile() throws IOException {
        RawDataStore store = new RawDataStore(tempDir);
        String source = "apod";
        String initialJson = "{\"version\": 1}";
        String updatedJson = "{\"version\": 2, \"updated\": true}";

        store.save(source, initialJson);
        Path savedPath = store.saveAndGetPath(source, updatedJson);

        String content = Files.readString(savedPath, StandardCharsets.UTF_8);
        assertEquals(updatedJson, content, "O arquivo deve ser sobrescrito com a versão mais recente");
    }

    @Test
    void testLoadMostRecentReturnsEmptyWhenDirectoryDoesNotExist() {
        RawDataStore store = new RawDataStore(tempDir);
        Optional<CachedDataResult> result = store.loadMostRecent("inexistente");
        assertTrue(result.isEmpty(), "Deve retornar Optional.empty() se o diretório não existir");
    }

    @Test
    void testLoadMostRecentReturnsEmptyWhenDirectoryIsEmpty() throws IOException {
        Path emptySourceDir = tempDir.resolve("neows");
        Files.createDirectories(emptySourceDir);

        RawDataStore store = new RawDataStore(tempDir);
        Optional<CachedDataResult> result = store.loadMostRecent("neows");
        assertTrue(result.isEmpty(), "Deve retornar Optional.empty() se não houver arquivos json válidos");
    }

    @Test
    void testLoadMostRecentSelectsNewestDate() throws IOException {
        Path sourceDir = tempDir.resolve("celestrak");
        Files.createDirectories(sourceDir);

        Files.writeString(sourceDir.resolve("2026-09-10.json"), "{\"date\": \"2026-09-10\"}");
        Files.writeString(sourceDir.resolve("2026-09-20.json"), "{\"date\": \"2026-09-20\"}");
        Files.writeString(sourceDir.resolve("2026-09-15.json"), "{\"date\": \"2026-09-15\"}");

        RawDataStore store = new RawDataStore(tempDir);
        Optional<CachedDataResult> result = store.loadMostRecent("celestrak");

        assertTrue(result.isPresent());
        assertEquals("celestrak", result.get().source());
        assertEquals("{\"date\": \"2026-09-20\"}", result.get().rawJson());
        assertTrue(result.get().fromCache());
        assertEquals(LocalDate.of(2026, 9, 20), result.get().dataDoArquivo());
    }

    @Test
    void testSaveValidatesArguments() {
        RawDataStore store = new RawDataStore(tempDir);

        assertThrows(IllegalArgumentException.class, () -> store.save(null, "{}"));
        assertThrows(IllegalArgumentException.class, () -> store.save("", "{}"));
        assertThrows(IllegalArgumentException.class, () -> store.save("apod", null));
    }
}

