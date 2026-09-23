package com.rastreadorespacial.menu;

import com.rastreadorespacial.domain.RiskLevel;
import com.rastreadorespacial.domain.SpaceObject;
import com.rastreadorespacial.risk.RiskRanking;

import java.util.List;
import java.util.Map;

public final class RastrearObjetosScreen extends AbstractScreen {
    private boolean aguardandoNome;

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
        }
        navigator.output().println();
        navigator.output().println("1 - Ver todos ordenados por nível de perigo");
        navigator.output().println("2 - Ver agrupados por nível de risco");
        navigator.output().println("3 - Ver detalhes de um objeto específico");
        navigator.output().println("4 - Definir unidade de distância");
        navigator.output().println("0 - Voltar");
        navigator.output().println("Digite sua escolha:");
    }

    @Override
    public void tratarEscolha(String entrada) {
        if (aguardandoNome) {
            if ("0".equals(entrada)) {
                aguardandoNome = false;
                navigator.voltar();
                return;
            }
            mostrarDetalhesPorNome(entrada);
            aguardandoNome = false;
            return;
        }
        Integer escolha = numero(entrada, 4);
        if (escolha == null) return;
        switch (escolha) {
            case 0 -> navigator.voltar();
            case 1 -> imprimirOrdenados();
            case 2 -> imprimirAgrupados();
            case 3 -> {
                aguardandoNome = true;
                navigator.output().println("Digite o nome do objeto (ou 0 para voltar):");
            }
            case 4 -> context.alterarUnidadeDistancia(navigator);
            default -> { }
        }
    }

    private void imprimirOrdenados() {
        for (SpaceObject objeto : RiskRanking.ordenarPorPerigo(context.registry().listarTodos())) {
            navigator.output().println(MenuFormatters.risco(objeto, context.unidadeDistancia()));
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

    private void mostrarDetalhesPorNome(String nome) {
        if ("0".equals(nome)) return;
        context.registry().buscarPorNome(nome).ifPresentOrElse(objeto -> {
            navigator.output().println("Detalhes: " + MenuFormatters.objeto(objeto, context.unidadeDistancia()));
            navigator.output().println("ID canônico: " + objeto.getIdCanonico());
            navigator.output().println("Nível de risco: " + objeto.avaliarRisco().name());
            navigator.output().println("Tipo: " + objeto.getClass().getSimpleName());
        }, () -> navigator.output().println("Nenhum objeto encontrado com esse nome."));
    }
}
