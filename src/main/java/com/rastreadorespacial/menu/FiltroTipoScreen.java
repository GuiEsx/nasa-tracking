package com.rastreadorespacial.menu;

import com.rastreadorespacial.domain.Asteroid;
import com.rastreadorespacial.domain.Comet;
import com.rastreadorespacial.domain.Satellite;
import com.rastreadorespacial.domain.SpaceObject;

import java.util.List;
import java.util.function.Predicate;

public final class FiltroTipoScreen extends AbstractScreen {
    public FiltroTipoScreen(MenuNavigator navigator, MenuContext context) {
        super(navigator, context);
    }

    @Override
    public String getNomeExibicao() {
        return "Filtrar objetos próximos";
    }

    @Override
    public void exibir() {
        cabecalho();
        navigator.output().println("1 - Asteroide");
        navigator.output().println("2 - Cometa");
        navigator.output().println("3 - Satélite");
        navigator.output().println("0 - Voltar");
        navigator.output().println("Digite sua escolha:");
    }

    @Override
    public void tratarEscolha(String entrada) {
        Integer escolha = numero(entrada, 3);
        if (escolha == null) return;
        if (escolha == 0) {
            navigator.voltar();
            return;
        }
        Predicate<SpaceObject> filtro = switch (escolha) {
            case 1 -> Asteroid.class::isInstance;
            case 2 -> Comet.class::isInstance;
            case 3 -> Satellite.class::isInstance;
            default -> objeto -> false;
        };
        List<SpaceObject> filtrados = context.registry().listarTodos().stream().filter(filtro).toList();
        ObjetosProximosScreen.imprimirOrdenados(filtrados, navigator.output());
    }
}
