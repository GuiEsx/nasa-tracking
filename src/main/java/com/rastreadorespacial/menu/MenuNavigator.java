package com.rastreadorespacial.menu;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Scanner;

public final class MenuNavigator {
    private final Deque<Screen> pilha = new ArrayDeque<>();
    private final Scanner scanner;
    private final PrintStream output;
    private boolean executando = true;
    private boolean telaJaExibida;

    public MenuNavigator(Screen telaInicial) {
        this(telaInicial, new Scanner(System.in), System.out);
    }

    public MenuNavigator(Screen telaInicial, Scanner scanner, PrintStream output) {
        this.scanner = Objects.requireNonNull(scanner, "O scanner não pode ser nulo.");
        this.output = Objects.requireNonNull(output, "A saída não pode ser nula.");
        if (telaInicial != null) {
            pilha.push(telaInicial);
        }
    }

    public MenuNavigator(Scanner scanner, PrintStream output) {
        this(null, scanner, output);
    }

    public void iniciar(Screen telaInicial) {
        if (!pilha.isEmpty()) {
            throw new IllegalStateException("A tela inicial já foi definida.");
        }
        pilha.push(Objects.requireNonNull(telaInicial, "A tela inicial não pode ser nula."));
    }

    public void irPara(Screen tela) {
        pilha.push(Objects.requireNonNull(tela, "A tela não pode ser nula."));
        limparTela();
        tela.exibir();
        telaJaExibida = true;
    }

    public void voltar() {
        if (pilha.size() > 1) {
            pilha.pop();
            limparTela();
            pilha.peek().exibir();
            telaJaExibida = true;
        }
    }

    public void encerrar() {
        executando = false;
        pilha.clear();
    }

    public void loop() {
        while (executando && !pilha.isEmpty()) {
            if (!telaJaExibida) {
                pilha.peek().exibir();
            }
            telaJaExibida = false;
            if (!scanner.hasNextLine()) {
                output.println("Entrada encerrada. Use a opção 6 para sair quando estiver no modo interativo.");
                break;
            }
            pilha.peek().tratarEscolha(scanner.nextLine().trim());
        }
    }

    public Screen telaAtual() {
        return pilha.peek();
    }

    public int profundidade() {
        return pilha.size();
    }

    public boolean estaExecutando() {
        return executando;
    }

    public Deque<Screen> caminho() {
        return new ArrayDeque<>(pilha);
    }

    public void exibirBreadcrumb() {
        StringBuilder breadcrumb = new StringBuilder();
        Screen[] telas = pilha.toArray(Screen[]::new);
        for (int index = telas.length - 1; index >= 0; index--) {
            if (breadcrumb.length() > 0) {
                breadcrumb.append(" > ");
            }
            breadcrumb.append(telas[index].getNomeExibicao());
        }
        output.println(breadcrumb);
    }

    public PrintStream output() {
        return output;
    }

    public Scanner scanner() {
        return scanner;
    }

    private void limparTela() {
        output.println();
        output.println("========================================");
        output.println();
    }
}
