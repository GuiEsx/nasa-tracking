package com.rastreadorespacial.domain;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

/**
 * Formatador responsável por gerar resumos textuais legíveis a partir de um {@link DailySnapshot}.
 */
public final class SnapshotFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

    private SnapshotFormatter() {
        // Classe utilitária estática
    }

    /**
     * Gera um resumo textual em uma única linha sobre o retrato diário de objetos rastreados.
     *
     * <p>Exemplo de saída:
     * {@code "Retrato de 19/07/2026: 12 objetos. Mais próximo: 2026-AB a 0,8 distâncias lunares. Mais perigoso: 2024-XY (ALTO)."}</p>
     *
     * @param snapshot retrato diário a ser formatado
     * @return string formatada com o resumo analítico
     */
    public static String resumoTextual(DailySnapshot snapshot) {
        if (snapshot == null || snapshot.quantidadeDeObjetos() == 0) {
            return "Nenhum objeto rastreado nesta data.";
        }

        String dataFormatada = snapshot.getData().format(DATE_FORMATTER);
        int totalObjetos = snapshot.quantidadeDeObjetos();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Retrato de %s: %d objetos.", dataFormatada, totalObjetos));

        // Objeto mais próximo
        Optional<SpaceObject> proximoOpt = snapshot.maisProximo();
        if (proximoOpt.isPresent()) {
            SpaceObject proximo = proximoOpt.get();
            if (proximo instanceof HasDistance hd && hd.getDistanciaDeReferencia().isPresent()) {
                Distance dist = hd.getDistanciaDeReferencia().get();
                String distStr = String.format(LOCALE_PT_BR, "%.1f distâncias lunares", dist.emDistanciasLunares());
                sb.append(String.format(" Mais próximo: %s a %s.", proximo.getNome(), distStr));
            } else {
                sb.append(String.format(" Mais próximo: %s.", proximo.getNome()));
            }
        }

        // Objeto mais perigoso
        Optional<SpaceObject> perigosoOpt = snapshot.maisPerigoso();
        if (perigosoOpt.isPresent()) {
            SpaceObject perigoso = perigosoOpt.get();
            sb.append(String.format(" Mais perigoso: %s (%s).", perigoso.getNome(), perigoso.avaliarRisco().name()));
        }

        return sb.toString();
    }
}

