package com.rastreadorespacial.menu;

public final class MainMenuScreen extends AbstractScreen {
    public MainMenuScreen(MenuNavigator navigator, MenuContext context) {
        super(navigator, context);
    }

    @Override
    public String getNomeExibicao() {
        return "Início";
    }

    @Override
    public void exibir() {
        cabecalho();
        navigator.output().println("1 - Ver os objetos mais próximos");
        navigator.output().println("2 - Rastrear objetos (RF5: Classificação unificada)");
        navigator.output().println("3 - Consultar Registros diários (RF6: Posição via Localizável)");
        navigator.output().println("4 - Retrato diário com agregados (RF7 & RF9: Deduplicação)");
        navigator.output().println("5 - Consultar Feeds da NASA (RF10 & RF8: Resiliência)");
        navigator.output().println("6 - Sair do sistema");
        navigator.output().println("Digite sua escolha:");
    }

    @Override
    public void tratarEscolha(String entrada) {
        Integer escolha;
        try {
            escolha = Integer.parseInt(entrada);
        } catch (NumberFormatException e) {
            navigator.output().println("Opção inválida. Digite um número entre 1 e 6.");
            return;
        }
        switch (escolha) {
            case 1 -> navigator.irPara(new ObjetosProximosScreen(navigator, context));
            case 2 -> navigator.irPara(new RastrearObjetosScreen(navigator, context));
            case 3 -> navigator.irPara(new RegistrosDiariosScreen(navigator, context));
            case 4 -> navigator.irPara(new RetratoDiarioScreen(navigator, context));
            case 5 -> navigator.irPara(new ConsultarFeedsScreen(navigator, context));
            case 6 -> {
                navigator.output().println("Encerrando o Rastreador Espacial. Até logo!");
                navigator.encerrar();
            }
            default -> navigator.output().println("Opção inválida. Digite um número entre 1 e 6.");
        }
    }
}
