package service;

import model.Cometa;
import model.Distancia;
import util.ApiHttpUtil;

import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;

public class ClienteNasaSbdb {

    public static Cometa buscarCometaHalley() {
        try {
            String urlSbdb = "https://ssd-api.jpl.nasa.gov/sbdb.api?sstr=1P";
            URL url = URI.create(urlSbdb).toURL();
            String textoJson = ApiHttpUtil.lerResposta(url, "Mozilla/5.0");

            int indexNome = textoJson.indexOf("\"fullname\":\"") + 12;
            String nome = textoJson.substring(indexNome, textoJson.indexOf("\"", indexNome));

            int indexUltimoObs = textoJson.indexOf("\"last_obs\":\"") + 13;
            String ultimoObsTexto = textoJson.substring(indexUltimoObs, textoJson.indexOf("\"", indexUltimoObs));
            LocalDateTime ultimoAvistamento = LocalDateTime.parse(ultimoObsTexto + "T00:00:00");

            double tamanhoNucleoKm = 11.0;
            Distancia distanciaPerielio = Distancia.deDistanciasLunares(5.0);

            return new Cometa("1P", nome, distanciaPerielio, tamanhoNucleoKm, ultimoAvistamento);

        } catch (Exception e) {
            System.out.println("Falha ao buscar o Cometa na API. Usando backup local do projeto.");
            Cometa backup = BackupDadosService.carregarCometa();
            if (backup != null) {
                return backup;
            }
            return RastreadorService.criarCometaFallback();
        }
    }
}
