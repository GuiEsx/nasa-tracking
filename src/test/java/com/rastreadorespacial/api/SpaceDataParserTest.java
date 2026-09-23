package com.rastreadorespacial.api;

import com.rastreadorespacial.domain.Comet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpaceDataParserTest {
    @Test
    void converteRespostaSbdbEmCometa() {
        String json = """
                [{
                  "object": {"kind": "cn", "des": "67P", "fullname": "67P/Churyumov-Gerasimenko"},
                  "orbit": {"elements": [{"name": "q", "value": "1.24"}]},
                  "phys_par": [{"name": "diameter", "value": "4.1"}]
                }]
                """;

        List<Comet> cometas = SpaceDataParser.parseSbdb(json);

        assertEquals(1, cometas.size());
        assertEquals("67P", cometas.get(0).getId());
        assertEquals(4.1, cometas.get(0).getDiametroNucleoKm());
        assertEquals(1.24 * 149_597_870.7, cometas.get(0).getDistanciaPerielio().emKilometros(), 0.001);
    }
}