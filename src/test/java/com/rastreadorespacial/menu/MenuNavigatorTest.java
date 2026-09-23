package com.rastreadorespacial.menu;

import com.rastreadorespacial.api.RetryPolicy;
import com.rastreadorespacial.api.SpaceDataClient;
import com.rastreadorespacial.cache.RawDataStore;
import com.rastreadorespacial.cache.SpaceDataService;
import com.rastreadorespacial.domain.Asteroid;
import com.rastreadorespacial.domain.Distance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuNavigatorTest {
    @TempDir
    Path tempDir;

    @Test
    void empilhaDesempilhaEEncerra() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        Screen inicio = new DummyScreen("Início");
        MenuNavigator navigator = new MenuNavigator(inicio, new Scanner(""), new PrintStream(bytes));
        Screen segunda = new DummyScreen("Segunda");

        navigator.irPara(segunda);
        assertEquals(2, navigator.profundidade());
        assertEquals(segunda, navigator.telaAtual());

        navigator.voltar();
        assertEquals(1, navigator.profundidade());
        assertEquals(inicio, navigator.telaAtual());

        navigator.encerrar();
        assertFalse(navigator.estaExecutando());
        assertEquals(0, navigator.profundidade());
    }

    @Test
    void menuPrincipalNavegaERejeitaEntradaInvalida() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MenuContext context = context(bytes);
        MenuNavigator navigator = new MenuNavigator(null, new Scanner(""), new PrintStream(bytes));
        MainMenuScreen main = new MainMenuScreen(navigator, context);
        navigator.iniciar(main);

        main.tratarEscolha("abc");
        assertEquals(main, navigator.telaAtual());
        assertTrue(bytes.toString().contains("Opção inválida"));
        main.tratarEscolha("1");
        assertTrue(navigator.telaAtual() instanceof ObjetosProximosScreen);
    }

    @Test
    void zeroVoltaEmTodasAsTelasComSubmenu() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MenuContext context = context(bytes);
        MenuNavigator navigator = new MenuNavigator(null, new Scanner(""), new PrintStream(bytes));
        MainMenuScreen main = new MainMenuScreen(navigator, context);
        navigator.iniciar(main);

        Screen[] telas = {
                new ObjetosProximosScreen(navigator, context),
                new FiltroTipoScreen(navigator, context),
                new RastrearObjetosScreen(navigator, context),
                new RegistrosDiariosScreen(navigator, context),
                new RetratoDiarioScreen(navigator, context),
                new ConsultarFeedsScreen(navigator, context),
                new AtualizarFeedScreen(navigator, context)
        };
        for (Screen tela : telas) {
            navigator.irPara(tela);
            tela.tratarEscolha("0");
            assertEquals(main, navigator.telaAtual(), tela.getNomeExibicao());
        }
    }

    @Test
    void objetosProximosSoImprimeObjetosAposEscolha() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MenuContext context = context(bytes);
        context.registry().adicionar(new Asteroid("1", "Asteroide de teste", Distance.ofKilometers(100), false));
        bytes.reset();
        MenuNavigator navigator = new MenuNavigator(new DummyScreen("Início"), new Scanner(""), new PrintStream(bytes));
        ObjetosProximosScreen screen = new ObjetosProximosScreen(navigator, context);

        screen.exibir();
        assertFalse(bytes.toString().contains("Asteroide de teste"));

        screen.tratarEscolha("1");
        assertTrue(bytes.toString().contains("Asteroide de teste"));
    }

    @Test
    void rastrearObjetosSoImprimeObjetosAposEscolha() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        MenuContext context = context(bytes);
        context.registry().adicionar(new Asteroid("1", "Asteroide de teste", Distance.ofKilometers(100), false));
        bytes.reset();
        MenuNavigator navigator = new MenuNavigator(new DummyScreen("Início"), new Scanner(""), new PrintStream(bytes));
        RastrearObjetosScreen screen = new RastrearObjetosScreen(navigator, context);

        screen.exibir();
        assertFalse(bytes.toString().contains("Asteroide de teste"));

        screen.tratarEscolha("1");
        assertTrue(bytes.toString().contains("Asteroide de teste"));
    }

    private MenuContext context(ByteArrayOutputStream bytes) {
        SpaceDataClient client = () -> "{}";
        SpaceDataService service = new SpaceDataService(
                new RawDataStore(tempDir), new RetryPolicy(Duration.ZERO, ignored -> {}), 1);
        return new MenuContext(service, client, client, client, new PrintStream(bytes));
    }

    private record DummyScreen(String getNomeExibicao) implements Screen {
        @Override
        public void exibir() {}

        @Override
        public void tratarEscolha(String entrada) {}
    }
}
