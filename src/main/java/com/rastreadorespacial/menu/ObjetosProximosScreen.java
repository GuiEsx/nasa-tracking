package com.rastreadorespacial.menu;

import com.rastreadorespacial.domain.HasDistance;
import com.rastreadorespacial.domain.SpaceObject;
import com.rastreadorespacial.domain.DistanceUnit;

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
        navigator.output().println("1 - Ver todos ordenados por proximidade");
        navigator.output().println("2 - Ver apenas o mais próximo");
        navigator.output().println("3 - Filtrar por tipo");
        navigator.output().println("4 - Definir unidade de distância");
        navigator.output().println("0 - Voltar");
        navigator.output().println("Digite sua escolha:");
    }

    @Override
    public void tratarEscolha(String entrada) {
        Integer escolha = numero(entrada, 4);
        if (escolha == null) return;
        switch (escolha) {
            case 0 -> navigator.voltar();
            case 1 -> imprimirOrdenados(context.registry().listarTodos());
            case 2 -> imprimirMaisProximo();
            case 3 -> navigator.irPara(new FiltroTipoScreen(navigator, context));
            case 4 -> context.alterarUnidadeDistancia(navigator);
            default -> { }
        }
    }

    static void imprimirOrdenados(List<SpaceObject> objetos, java.io.PrintStream output, MenuContext context) {
        DistanceUnit unidade = context.unidadeDistancia();
        List<SpaceObject> ordenados = objetos.stream()
                .filter(objeto -> objeto instanceof HasDistance)
                .filter(objeto -> ((HasDistance) objeto).getDistanciaDeReferencia().isPresent())
                .sorted(Comparator.comparing(objeto -> ((HasDistance) objeto)
                        .getDistanciaDeReferencia().orElseThrow()))
                .toList();
        if (ordenados.isEmpty()) {
            if (objetos.isEmpty()) {
                output.println("Nenhum objeto foi carregado. Atualize os feeds da NASA antes de consultar proximidade.");
            } else {
                output.println("Nenhum objeto com distância aplicável foi carregado. Satélites e outros objetos sem distância de referência não aparecem aqui. Use 'Rastrear objetos' para visualizar os satélites carregados.");
            }
            return;
        }
        for (SpaceObject objeto : ordenados) {
            output.println(" - " + MenuFormatters.distancia(objeto, unidade));
        }
    }

    private void imprimirOrdenados(List<SpaceObject> objetos) {
        imprimirOrdenados(objetos, navigator.output(), context);
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
