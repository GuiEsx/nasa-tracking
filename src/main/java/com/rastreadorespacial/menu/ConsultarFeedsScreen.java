package com.rastreadorespacial.menu;

import com.rastreadorespacial.cache.CachedDataResult;

import java.util.Map;

public final class ConsultarFeedsScreen extends AbstractScreen {
    public ConsultarFeedsScreen(MenuNavigator navigator, MenuContext context) {
        super(navigator, context);
    }

    @Override
    public String getNomeExibicao() {
        return "Feeds da NASA";
    }

    @Override
    public void exibir() {
        cabecalho();
        navigator.output().println("1 - Atualizar todos os feeds agora");
        navigator.output().println("2 - Atualizar um feed específico");
        navigator.output().println("3 - Ver status do último carregamento de cada feed");
        navigator.output().println("0 - Voltar");
        navigator.output().println("Digite sua escolha:");
    }

    @Override
    public void tratarEscolha(String entrada) {
        Integer escolha = numero(entrada, 3);
        if (escolha == null) return;
        switch (escolha) {
            case 0 -> navigator.voltar();
            case 1 -> {
                int novos = context.atualizarTodos();
                navigator.output().println("Atualização concluída. Objetos novos adicionados após deduplicação: " + novos);
            }
            case 2 -> navigator.irPara(new AtualizarFeedScreen(navigator, context));
            case 3 -> imprimirStatus();
            default -> { }
        }
    }

    private void imprimirStatus() {
        Map<String, CachedDataResult> status = context.ultimoCarregamento();
        for (String source : new String[]{"neows", "apod", "celestrak", "sbdb"}) {
            CachedDataResult result = status.get(source);
            if (result == null) {
                navigator.output().println(source + ": ainda não carregado.");
            } else {
                String origem = result.fromCache() ? "cache" : "API";
                navigator.output().println(String.format("%s: %s de %s, %d tentativa(s)",
                        source, origem, result.dataDoArquivo(), result.attempts()));
            }
        }
    }
}
