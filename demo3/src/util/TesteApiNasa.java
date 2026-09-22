package util;

import java.net.URI;
import java.net.URL;

public class TesteApiNasa {
    public static void main(String[] args) {
        try {
            String urlNasa = "https://api.nasa.gov/neo/rest/v1/feed?start_date=2026-08-01&api_key=DEMO_KEY";
            URL url = URI.create(urlNasa).toURL();
            
            String resposta = ApiHttpUtil.lerResposta(url, null);

            System.out.println("Resposta da NASA:\n" + resposta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
