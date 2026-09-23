package com.rastreadorespacial.menu;

import com.rastreadorespacial.domain.RiskLevel;
import com.rastreadorespacial.domain.SpaceObject;
import com.rastreadorespacial.risk.RiskRanking;

import java.util.List;
import java.util.Map;

public final class RastrearObjetosScreen extends AbstractScreen {
    private boolean aguardandoId;

    public RastrearObjetosScreen(MenuNavigator navigator, MenuContext context) {
        super(navigator, context);
    }

    @Override
    public String getNomeExibicao() {
        return "Rastrear objetos";
    }

    @Override
    public void exibir() {
        cabecalho();
        if (context.registry().quantidade() == 0) {
            navigator.output().println("Nenhum objeto carregado. Acesse 'Consultar Feeds da NASA' primeiro.");
        } else {
            imprimirOrdenados();
        }
        navigator.output().println();
        navigator.output().println("1 - Ver todos ordenados por nível de perigo");
        navigator.output().println("2 - Ver agrupados por nível de risco");
        navigator.output().println("3 - Ver detalhes de um objeto específico");
        navigator.output().println("0 - Voltar");
        navigator.output().println("Digite sua escolha:");
    }

    @Override
    public void tratarEscolha(String entrada) {
        if (aguardandoId) {
            if ("0".equals(entrada)) {
                aguardandoId = false;
                navigator.voltar();
                return;
            }
            mostrarDetalhes(entrada);
            aguardandoId = false;
            return;
        }
        Integer escolha = numero(entrada, 3);
        if (escolha == null) return;
        switch (escolha) {
            case 0 -> navigator.voltar();
            case 1 -> imprimirOrdenados();
            case 2 -> imprimirAgrupados();
            case 3 -> {
                aguardandoId = true;
                navigator.output().println("Digite o ID canônico do objeto (ou 0 para voltar):");
            }
            default -> { }
        }
    }

    private void imprimirOrdenados() {
        for (SpaceObject objeto : RiskRanking.ordenarPorPerigo(context.registry().listarTodos())) {
            navigator.output().println(MenuFormatters.risco(objeto));
        }
    }

    private void imprimirAgrupados() {
        Map<RiskLevel, List<SpaceObject>> grupos = RiskRanking.agruparPorNivel(context.registry().listarTodos());
        for (RiskLevel level : RiskLevel.values()) {
            navigator.output().println(level.name() + ":");
            for (SpaceObject objeto : grupos.getOrDefault(level, List.of())) {
                navigator.output().println(" - " + objeto.getNome());
            }
        }
    }

    private void mostrarDetalhes(String id) {
        if ("0".equals(id)) return;
        context.registry().buscarPorIdCanonico(id).ifPresentOrElse(objeto -> {
            navigator.output().println("Detalhes: " + MenuFormatters.objeto(objeto));
            navigator.output().println("ID canônico: " + objeto.getIdCanonico());
            navigator.output().println("Nível de risco: " + objeto.avaliarRisco().name());
            navigator.output().println("Tipo: " + objeto.getClass().getSimpleName());
        }, () -> navigator.output().println("Nenhum objeto encontrado com esse ID canônico."));
    }
}
