package com.rastreadorespacial.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceTest {

    private static final double EPSILON = 1e-4;

    @Test
    void testCreationAndConversionFromKilometers() {
        Distance d = Distance.ofKilometers(384_400.0);

        assertEquals(384_400.0, d.emQuilometros(), EPSILON);
        assertEquals(1.0, d.emDistanciasLunares(), EPSILON);
        assertEquals(384_400.0 / 1.609344, d.emMilhas(), EPSILON);
    }

    @Test
    void testCreationAndConversionFromMiles() {
        Distance d = Distance.ofMiles(100.0);

        assertEquals(160.9344, d.emQuilometros(), EPSILON);
        assertEquals(100.0, d.emMilhas(), EPSILON);
        assertEquals(160.9344 / 384_400.0, d.emDistanciasLunares(), EPSILON);
    }

    @Test
    void testCreationAndConversionFromLunarDistances() {
        Distance d = Distance.ofLunarDistances(2.5);

        assertEquals(2.5 * 384_400.0, d.emQuilometros(), EPSILON);
        assertEquals((2.5 * 384_400.0) / 1.609344, d.emMilhas(), EPSILON);
        assertEquals(2.5, d.emDistanciasLunares(), EPSILON);
    }

    @Test
    void testEqualsAndHashCodeAcrossDifferentUnits() {
        Distance fromLd = Distance.ofLunarDistances(1.0);
        Distance fromKm = Distance.ofKilometers(384_400.0);

        assertEquals(fromKm, fromLd, "1 LD e 384400 km devem ser considerados iguais");
        assertEquals(fromKm.hashCode(), fromLd.hashCode(), "HashCodes devem ser consistentes com equals()");

        Distance fromMiles = Distance.ofMiles(10.0);
        Distance fromKmEquivalent = Distance.ofKilometers(16.09344);
        assertEquals(fromMiles, fromKmEquivalent);
        assertEquals(fromMiles.hashCode(), fromKmEquivalent.hashCode());
    }

    @Test
    void testIsMenorQueAcrossDifferentUnits() {
        Distance small = Distance.ofKilometers(100.0);
        Distance large = Distance.ofLunarDistances(1.0); // 384,400 km

        assertTrue(small.isMenorQue(large));
        assertFalse(large.isMenorQue(small));
        assertFalse(small.isMenorQue(small));

        Distance tenMiles = Distance.ofMiles(10.0); // 16.09344 km
        Distance twentyKm = Distance.ofKilometers(20.0);
        assertTrue(tenMiles.isMenorQue(twentyKm));
        assertTrue(twentyKm.isMaiorQue(tenMiles));
    }

    @Test
    void testComparable() {
        Distance d1 = Distance.ofKilometers(100);
        Distance d2 = Distance.ofKilometers(200);
        Distance d3 = Distance.ofMiles(100 / 1.609344); // ~100 km

        assertTrue(d1.compareTo(d2) < 0);
        assertTrue(d2.compareTo(d1) > 0);
        assertEquals(0, d1.compareTo(d3));
    }

    @Test
    void testToStringFormat() {
        Distance d = Distance.ofKilometers(1234.567);
        assertEquals("1234.57 km", d.toString());
    }

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> Distance.ofKilometers(-1.0));
        assertThrows(IllegalArgumentException.class, () -> Distance.ofKilometers(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Distance.ofKilometers(Double.POSITIVE_INFINITY));
        assertThrows(NullPointerException.class, () -> Distance.of(10, null));
    }
}

