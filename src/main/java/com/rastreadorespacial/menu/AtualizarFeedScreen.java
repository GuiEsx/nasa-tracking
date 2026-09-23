package com.rastreadorespacial.menu;

public final class AtualizarFeedScreen extends AbstractScreen {
    public AtualizarFeedScreen(MenuNavigator navigator, MenuContext context) {
        super(navigator, context);
    }

    @Override
    public String getNomeExibicao() {
        return "Atualizar feed";
    }

    @Override
    public void exibir() {
        cabecalho();
        navigator.output().println("1 - NeoWs");
        navigator.output().println("2 - APOD");
        navigator.output().println("3 - Celestrak");
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
        String source = switch (escolha) {
            case 1 -> "neows";
            case 2 -> "apod";
            case 3 -> "celestrak";
            default -> "";
        };
        context.atualizar(source);
    }
}
