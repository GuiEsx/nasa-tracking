package com.rastreadorespacial.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @Test
    void testDirectoriesCreation() {
        AppConfig.initDirectories();
        assertTrue(Files.exists(AppConfig.DATA_DIR), "Diretório base de dados deve existir");
        assertTrue(Files.exists(AppConfig.NEOWS_DATA_DIR), "Subdiretório neows deve existir");
        assertTrue(Files.exists(AppConfig.APOD_DATA_DIR), "Subdiretório apod deve existir");
        assertTrue(Files.exists(AppConfig.CELESTRAK_DATA_DIR), "Subdiretório celestrak deve existir");
    }

    @Test
    void testBaseUrlsAreDefined() {
        assertNotNull(AppConfig.NEOWS_BASE_URL);
        assertNotNull(AppConfig.APOD_BASE_URL);
        assertNotNull(AppConfig.CELESTRAK_BASE_URL);
    }
}

