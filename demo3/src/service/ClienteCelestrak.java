package service;

import model.Distancia;
import model.Satelite;
import util.ApiHttpUtil;

import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClienteCelestrak {

    public static Satelite buscarEstacaoEspacial() {
        try {
            String urlSatelite = "https://celestrak.org/NORAD/elements/gp.php?CATNR=25544&FORMAT=json";
            URL url = URI.create(urlSatelite).toURL();
            String textoJson = ApiHttpUtil.lerResposta(url, "Mozilla/5.0");

            int indexNome = textoJson.indexOf("\"OBJECT_NAME\":\"") + 15;
            String nome = textoJson.substring(indexNome, textoJson.indexOf("\"", indexNome));

            int indexEpoch = textoJson.indexOf("\"EPOCH\":\"") + 10;
            String epochTexto = textoJson.substring(indexEpoch, textoJson.indexOf("\"", indexEpoch));
            LocalDateTime ultimoAvistamento = LocalDateTime.parse(epochTexto, DateTimeFormatter.ISO_DATE_TIME);

            Distancia altitudeAtual = Distancia.deMetros(420000.0);
            double decaimentoSimulado = 8.5;

            return new Satelite("25544", nome, altitudeAtual, decaimentoSimulado, ultimoAvistamento);

        } catch (Exception e) {
            System.out.println("Falha ao buscar o Satélite na API. Usando backup local do projeto.");
            Satelite backup = BackupDadosService.carregarSatelite();
            if (backup != null) {
                return backup;
            }
            return RastreadorService.criarSateliteFallback();
        }
    }
}
