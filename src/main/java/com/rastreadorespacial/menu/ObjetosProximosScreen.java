package com.rastreadorespacial.menu;

import com.rastreadorespacial.domain.HasDistance;
import com.rastreadorespacial.domain.SpaceObject;

import java.util.Comparator;
import java.util.List;

public final class ObjetosProximosScreen extends AbstractScreen {
    public ObjetosProximosScreen(MenuNavigator navigator, MenuContext context) {
        super(navigator, context);
    }

    @Override
    public String getNomeExibicao() {
        return "Objetos mais próximos";
    }

    @Override
    public void exibir() {
        cabecalho();
        navigator.output().println("Objetos com distância aplicável:");
        imprimirOrdenados(context.registry().listarTodos());
        navigator.output().println();
        navigator.output().println("1 - Ver todos ordenados por proximidade");
        navigator.output().println("2 - Ver apenas o mais próximo");
        navigator.output().println("3 - Filtrar por tipo");
        navigator.output().println("0 - Voltar");
        navigator.output().println("Digite sua escolha:");
    }

    @Override
    public void tratarEscolha(String entrada) {
        Integer escolha = numero(entrada, 3);
        if (escolha == null) return;
        switch (escolha) {
            case 0 -> navigator.voltar();
            case 1 -> imprimirOrdenados(context.registry().listarTodos());
            case 2 -> imprimirMaisProximo();
            case 3 -> navigator.irPara(new FiltroTipoScreen(navigator, context));
            default -> { }
        }
    }

    static void imprimirOrdenados(List<SpaceObject> objetos, java.io.PrintStream output) {
        List<SpaceObject> ordenados = objetos.stream()
                .filter(objeto -> objeto instanceof HasDistance)
                .filter(objeto -> ((HasDistance) objeto).getDistanciaDeReferencia().isPresent())
                .sorted(Comparator.comparing(objeto -> ((HasDistance) objeto)
                        .getDistanciaDeReferencia().orElseThrow()))
                .toList();
        if (ordenados.isEmpty()) {
            output.println("Nenhum objeto com distância aplicável foi carregado.");
            return;
        }
        for (SpaceObject objeto : ordenados) {
            output.println(" - " + MenuFormatters.distancia(objeto));
        }
    }

    private void imprimirOrdenados(List<SpaceObject> objetos) {
        imprimirOrdenados(objetos, navigator.output());
    }

    private void imprimirMaisProximo() {
        List<SpaceObject> ordenados = context.registry().listarTodos().stream()
                .filter(objeto -> objeto instanceof HasDistance)
                .filter(objeto -> ((HasDistance) objeto).getDistanciaDeReferencia().isPresent())
                .sorted(Comparator.comparing(objeto -> ((HasDistance) objeto)
                        .getDistanciaDeReferencia().orElseThrow()))
                .limit(1)
                .toList();
        imprimirOrdenados(ordenados);
    }
}
