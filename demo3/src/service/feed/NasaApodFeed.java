package service.feed;

import model.ApodRegistro;
import util.ApiHttpUtil;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NasaApodFeed implements NasaFeed<ApodRegistro> {
    private final LocalDate data;
    private final String apiKey;

    public NasaApodFeed() {
        this(LocalDate.now(), "DEMO_KEY");
    }

    public NasaApodFeed(LocalDate data, String apiKey) {
        this.data = data;
        this.apiKey = (apiKey != null && !apiKey.trim().isEmpty()) ? apiKey.trim() : "DEMO_KEY";
    }

    @Override
    public String getNomeFeed() {
        return "NASA APOD (Astronomy Picture of the Day)";
    }

    @Override
    public String getEndpointUrl() {
        // Se a data for hoje, omitir o parâmetro date para evitar problemas de fuso horário na API da NASA
        String parametroData = "";
        if (data != null && !data.equals(LocalDate.now())) {
            parametroData = "&date=" + data.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return "https://api.nasa.gov/planetary/apod?api_key=" + apiKey + parametroData;
    }

    @Override
    public ApodRegistro consultarFeed() throws Exception {
        try {
            URL url = URI.create(getEndpointUrl()).toURL();
            String json = ApiHttpUtil.lerResposta(url, "Mozilla/5.0");

            String titulo = extrairCampoTexto(json, "title");
            String explicacao = extrairCampoTexto(json, "explanation");
            String urlMidia = extrairCampoTexto(json, "url");
            String dataStr = extrairCampoTexto(json, "date");

            LocalDate dataRegistro = (dataStr != null && !dataStr.isEmpty())
                    ? LocalDate.parse(dataStr)
                    : LocalDate.now();

            return new ApodRegistro(
                    (titulo != null && !titulo.isEmpty()) ? titulo : "Imagem Astronômica do Dia",
                    (explicacao != null) ? explicacao : "Sem descrição disponível.",
                    (urlMidia != null) ? urlMidia : "https://apod.nasa.gov/",
                    dataRegistro
            );

        } catch (exception.RateLimitExceededException rle) {
            System.err.println("\n[RF8 - ATENÇÃO] Limite de requisições da NASA excedido (HTTP 429) no feed APOD!");
            System.err.printf("[RF8 - Informação] O sistema tentou %d vezes com retentativa progressiva (esperou %d s) e não travou.%n",
                    rle.getTentativasRealizadas(), (rle.getTempoTotalEsperaMs() / 1000));
            System.err.println("[RF8 - Informação] Desistindo da consulta online para garantir a fluidez da aplicação.");
            System.out.println("-> Acionando contingência: exibindo imagem astronômica de backup local...");
            return new ApodRegistro(
                    "O Coração da Nebulosa de Órion (Backup Local)",
                    "A nebulosa M42 em Órion é um berçário estelar a 1.344 anos-luz da Terra. Imagem exibida via contingência local devido ao limite de taxa da API da NASA.",
                    "https://apod.nasa.gov/apod/image/orion_nebula.jpg",
                    (data != null) ? data : LocalDate.now()
            );
        } catch (Exception e) {
            System.out.println("[NASA APOD] Falha ao consultar feed online: " + e.getMessage() + ". Utilizando registro local de fallback.");
            return new ApodRegistro(
                    "O Coração da Nebulosa de Órion",
                    "A nebulosa M42 em Órion é um dos berçários estelares mais brilhantes da nossa galáxia, repleta de estrelas jovens e gás luminoso.",
                    "https://apod.nasa.gov/apod/image/orion_nebula.jpg",
                    (data != null) ? data : LocalDate.now()
            );
        }
    }

    private String extrairCampoTexto(String json, String campo) {
        String chave = "\"" + campo + "\":\"";
        int inicio = json.indexOf(chave);
        if (inicio == -1) {
            return null;
        }
        inicio += chave.length();
        int fim = json.indexOf("\"", inicio);
        if (fim == -1) {
            return null;
        }
        return json.substring(inicio, fim).replace("\\\"", "\"").replace("\\n", "\n");
    }
}
