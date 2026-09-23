package com.rastreadorespacial.menu;

import java.util.Objects;

abstract class AbstractScreen implements Screen {
    protected final MenuNavigator navigator;
    protected final MenuContext context;

    protected AbstractScreen(MenuNavigator navigator, MenuContext context) {
        this.navigator = Objects.requireNonNull(navigator);
        this.context = Objects.requireNonNull(context);
    }

    protected void cabecalho() {
        navigator.exibirBreadcrumb();
    }

    protected Integer numero(String entrada, int maximo) {
        try {
            int valor = Integer.parseInt(entrada);
            if (valor < 0 || valor > maximo) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            navigator.output().println("Opção inválida. Digite um número entre 0 e " + maximo + ".");
            return null;
        }
    }

    protected void voltarOuAvisar(int escolha) {
        if (escolha == 0) {
            navigator.voltar();
        }
    }
}
