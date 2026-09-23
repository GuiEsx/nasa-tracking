package com.rastreadorespacial.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TrackedObjectRegistryTest {

    private TrackedObjectRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TrackedObjectRegistry();
    }

    @Test
    void testAdicionarDoisAsteroidesDiferentes() {
        Asteroid a1 = new Asteroid("3542519", "2010 PK9", Distance.ofLunarDistances(7.5), false);
        Asteroid a2 = new Asteroid("99942", "Apophis", Distance.ofLunarDistances(0.8), true);

        registry.adicionar(a1);
        registry.adicionar(a2);

        assertEquals(2, registry.quantidade(), "Devem existir 2 objetos distintos no registro");
        assertTrue(registry.listarTodos().contains(a1));
        assertTrue(registry.listarTodos().contains(a2));
    }

    @Test
    void testDeduplicacaoPorIdCanonicoMantemVersaoMaisRecente() {
        // Objeto vindo do feed do dia 1 (distância 15 LD)
        Asteroid dia1 = new Asteroid("3542519", "2010 PK9 (Dia 1)", Distance.ofLunarDistances(15.0), false);
        // Mesmo asteroide com medição atualizada no feed do dia 2 (distância 14.8 LD)
        Asteroid dia2 = new Asteroid("3542519", "2010 PK9 (Dia 2 - Atualizado)", Distance.ofLunarDistances(14.8), false);

        registry.adicionar(dia1);
        assertEquals(1, registry.quantidade());

        // Adiciona a versão do dia 2
        registry.adicionar(dia2);
        assertEquals(1, registry.quantidade(), "A quantidade deve permanecer 1 após registrar o mesmo asteroide");

        Optional<SpaceObject> resgatado = registry.buscarPorIdCanonico("3542519");
        assertTrue(resgatado.isPresent());
        assertEquals("2010 PK9 (Dia 2 - Atualizado)", resgatado.get().getNome(), "Deve ter sobrescrito com a versão mais recente");
        assertEquals(14.8, ((Asteroid) resgatado.get()).getDistanciaDePassagem().emDistanciasLunares(), 1e-4);
    }

    @Test
    void testAdicionarTodosColecaoMistaComIdsDuplicados() {
        Asteroid a1 = new Asteroid("AST-1", "Asteroide Alfa", Distance.ofLunarDistances(10.0), false);
        Asteroid a1Recente = new Asteroid("AST-1", "Asteroide Alfa (V2)", Distance.ofLunarDistances(9.8), false);

        Comet c1 = new Comet("COM-1", "Cometa Beta", Distance.ofLunarDistances(25.0), 3.0);
        Satellite s1 = new Satellite("25544", "ISS", 0.0001);
        Satellite s1Recente = new Satellite("25544", "ISS (TLE Novo)", 0.00012);

        List<SpaceObject> loteMisto = List.of(a1, c1, s1, a1Recente, s1Recente);

        registry.adicionarTodos(loteMisto);

        // Devem sobrar exatamente 3 objetos únicos: AST-1, COM-1 e 25544
        assertEquals(3, registry.quantidade(), "Coleção mista deve conter 3 objetos após deduplicação de 5 entradas");

        assertEquals("Asteroide Alfa (V2)", registry.buscarPorIdCanonico("AST-1").get().getNome());
        assertEquals("Cometa Beta", registry.buscarPorIdCanonico("COM-1").get().getNome());
        assertEquals("ISS (TLE Novo)", registry.buscarPorIdCanonico("25544").get().getNome());
    }

    @Test
    void testLimparEResilienciaEntradaNula() {
        registry.adicionar(null);
        registry.adicionarTodos(null);
        assertEquals(0, registry.quantidade());

        Asteroid a = new Asteroid("1", "Obj", Distance.ofKilometers(100), false);
        registry.adicionar(a);
        assertEquals(1, registry.quantidade());

        registry.limpar();
        assertEquals(0, registry.quantidade());
    }
}

