package com.rastreadorespacial.menu;

import com.rastreadorespacial.config.AppConfig;
import com.rastreadorespacial.domain.DailySnapshot;
import com.rastreadorespacial.domain.RiskLevel;
import com.rastreadorespacial.domain.SpaceObject;
import com.rastreadorespacial.domain.SnapshotFormatter;

import java.util.Map;
import java.util.Optional;

public final class RetratoDiarioScreen extends AbstractScreen {
    private final SnapshotHistoryStore historyStore = new SnapshotHistoryStore(AppConfig.DATA_DIR.resolve("snapshots"));

    public RetratoDiarioScreen(MenuNavigator navigator, MenuContext context) {
        super(navigator, context);
    }

    @Override
    public String getNomeExibicao() {
        return "Retrato diário";
    }

    @Override
    public void exibir() {
        cabecalho();
        navigator.output().println("1 - Ver resumo textual do dia");
        navigator.output().println("2 - Ver estatísticas detalhadas");
        navigator.output().println("3 - Ver histórico de retratos anteriores");
        navigator.output().println("4 - Ver foto e descrição do objeto do dia (APOD)");
        navigator.output().println("0 - Voltar");
        navigator.output().println("Digite sua escolha:");
    }

    @Override
    public void tratarEscolha(String entrada) {
        Integer escolha = numero(entrada, 4);
        if (escolha == null) return;
        switch (escolha) {
            case 0 -> navigator.voltar();
            case 1 -> {
                DailySnapshot snapshot = context.snapshot();
                historyStore.salvar(snapshot);
                navigator.output().println(SnapshotFormatter.resumoTextual(snapshot));
            }
            case 2 -> imprimirEstatisticas();
            case 3 -> imprimirHistorico();
            case 4 -> imprimirApod();
            default -> { }
        }
    }

    private void imprimirApod() {
        MenuContext.ApodInfo apod = context.apod();
        navigator.output().println("APOD - Foto astronômica do dia:");
        if (apod == null) {
            navigator.output().println("Dados do objeto do dia indisponíveis. Atualize o feed APOD primeiro.");
            return;
        }
        navigator.output().println("Título: " + apod.titulo());
        navigator.output().println("Data: " + apod.data());
        navigator.output().println("Descrição: " + apod.explicacao());
        if (!apod.url().isBlank()) {
            navigator.output().println("Imagem: " + apod.url());
        }
    }

    private void imprimirEstatisticas() {
        DailySnapshot snapshot = context.snapshot();
        historyStore.salvar(snapshot);
        navigator.output().println("Objetos ÚNICOS rastreados: " + snapshot.quantidadeDeObjetos());
        Optional<SpaceObject> proximo = snapshot.maisProximo();
        navigator.output().println("Mais próximo: " + proximo.map(MenuFormatters::distancia).orElse("nenhum"));
        navigator.output().println("Mais perigoso: " + proximoPerigoso(snapshot));
        navigator.output().println("Contagem por nível:");
        Map<RiskLevel, Long> contagens = snapshot.contagemPorNivelDeRisco();
        for (RiskLevel level : RiskLevel.values()) {
            navigator.output().println(" - " + level.name() + ": " + contagens.get(level));
        }
    }

    private String proximoPerigoso(DailySnapshot snapshot) {
        return snapshot.maisPerigoso()
                .map(objeto -> objeto.getNome() + " (" + objeto.avaliarRisco().name() + ")")
                .orElse("nenhum");
    }

    private void imprimirHistorico() {
        var datas = historyStore.listarDatas();
        if (datas.isEmpty()) {
            navigator.output().println("Nenhum retrato diário salvo ainda.");
            return;
        }
        navigator.output().println("Retratos salvos:");
        datas.forEach(data -> navigator.output().println(" - " + data));
    }
}
