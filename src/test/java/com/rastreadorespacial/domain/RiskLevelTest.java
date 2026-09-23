package com.rastreadorespacial.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskLevelTest {

    @Test
    void testFromStringValidValues() {
        assertEquals(RiskLevel.BAIXO, RiskLevel.fromString("BAIXO"));
        assertEquals(RiskLevel.MEDIO, RiskLevel.fromString("MEDIO"));
        assertEquals(RiskLevel.ALTO, RiskLevel.fromString("ALTO"));
        assertEquals(RiskLevel.CRITICO, RiskLevel.fromString("CRITICO"));

        // Case-insensitivity e espaços
        assertEquals(RiskLevel.BAIXO, RiskLevel.fromString("baixo"));
        assertEquals(RiskLevel.CRITICO, RiskLevel.fromString("  CrItIcO  "));
    }

    @Test
    void testFromStringInvalidValuesThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromString("GRAVE"));
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromString("EXTREMO"));
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromString("   "));
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromString(null));
    }

    @Test
    void testNaturalSeverityOrder() {
        assertTrue(RiskLevel.BAIXO.compareTo(RiskLevel.MEDIO) < 0, "BAIXO deve ser menos severo que MEDIO");
        assertTrue(RiskLevel.MEDIO.compareTo(RiskLevel.ALTO) < 0, "MEDIO deve ser menos severo que ALTO");
        assertTrue(RiskLevel.ALTO.compareTo(RiskLevel.CRITICO) < 0, "ALTO deve ser menos severo que CRITICO");
        assertTrue(RiskLevel.CRITICO.compareTo(RiskLevel.BAIXO) > 0, "CRITICO deve ser mais severo que BAIXO");
    }

    @Test
    void testDescricaoIsNotBlank() {
        for (RiskLevel level : RiskLevel.values()) {
            assertNotNull(level.getDescricao());
            assertFalse(level.getDescricao().isBlank(), "Cada nível de risco deve possuir descrição legível");
        }
    }
}

