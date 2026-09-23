package com.rastreadorespacial.menu;

import com.rastreadorespacial.domain.Locatable;
import com.rastreadorespacial.domain.Position;
import com.rastreadorespacial.domain.SpaceObject;
import com.rastreadorespacial.risk.PositionQuery;

import java.util.List;

public final class RegistrosDiariosScreen extends AbstractScreen {
    private boolean aguardandoNome;

    public RegistrosDiariosScreen(MenuNavigator navigator, MenuContext context) {
        super(navigator, context);
    }

    @Override
    public String getNomeExibicao() {
        return "Registros diários";
    }

    @Override
    public void exibir() {
        cabecalho();
        navigator.output().println("1 - Ver posição atual de todos os objetos localizáveis");
        navigator.output().println("2 - Ver posição atual de um objeto específico");
        navigator.output().println("3 - Ver objetos sem posição disponível");
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
            mostrarPosicaoPorNome(entrada);
            aguardandoNome = false;
            return;
        }
        Integer escolha = numero(entrada, 4);
        if (escolha == null) return;
        switch (escolha) {
            case 0 -> navigator.voltar();
            case 1 -> imprimirTodas();
            case 2 -> {
                aguardandoNome = true;
                navigator.output().println("Digite o nome do objeto (ou 0 para voltar):");
            }
            case 3 -> imprimirSemPosicao();
            case 4 -> context.alterarUnidadeDistancia(navigator);
            default -> { }
        }
    }

    private void imprimirTodas() {
        List<SpaceObject> objetos = context.registry().listarTodos();
        List<Position> posicoes = PositionQuery.posicoesAtuais(objetos);
        if (posicoes.isEmpty()) {
            navigator.output().println("Nenhum objeto localizável foi carregado.");
            return;
        }
        for (SpaceObject objeto : objetos) {
            if (objeto instanceof Locatable localizable) {
                navigator.output().println(" - " + MenuFormatters.posicao(objeto, localizable.getPosicaoAtual(), context.unidadeDistancia()));
            }
        }
    }

    private void mostrarPosicaoPorNome(String nome) {
        if ("0".equals(nome)) return;
        context.registry().buscarPorNome(nome).ifPresentOrElse(objeto -> {
            if (objeto instanceof Locatable localizable) {
                navigator.output().println(MenuFormatters.posicao(objeto, localizable.getPosicaoAtual(), context.unidadeDistancia()));
            } else {
                navigator.output().println("Este objeto não possui posição disponível; isso é esperado para alguns tipos.");
            }
        }, () -> navigator.output().println("Nenhum objeto encontrado com esse nome."));
    }

    private void imprimirSemPosicao() {
        List<SpaceObject> semPosicao = context.registry().listarTodos().stream()
                .filter(objeto -> !(objeto instanceof Locatable))
                .toList();
        if (semPosicao.isEmpty()) {
            navigator.output().println("Nenhum objeto sem posição disponível. A ausência de posição é esperada apenas para alguns tipos.");
            return;
        }
        navigator.output().println("Objetos sem posição disponível:");
        semPosicao.forEach(objeto -> navigator.output().println(" - " + objeto.getNome()));
    }
}
