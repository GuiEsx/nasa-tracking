package demo2; 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClienteNasaNeoWs {

    public static Asteroide buscarPrimeiroAsteroide() {
        try {
            // 1. Prepara a URL da NASA
            String urlNasa = "https://api.nasa.gov/neo/rest/v1/feed?start_date=2026-08-05&end_date=2026-08-05&api_key=DEMO_KEY";
            URL url = new URL(urlNasa);
            
            // 2. Faz o pedido na internet usando a moda antiga do Java 8
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("GET");
            
            // Lê o resultado em texto
            BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            StringBuilder resposta = new StringBuilder();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                resposta.append(linha);
            }
            leitor.close();
            
            String textoJson = resposta.toString();

            // 3. Lê o texto cru e "recorta" os dados
            int indexNome = textoJson.indexOf("\"name\":\"") + 8;
            String nome = textoJson.substring(indexNome, textoJson.indexOf("\"", indexNome));

            int indexPerigo = textoJson.indexOf("\"is_potentially_hazardous_asteroid\":") + 36;
            String perigoTexto = textoJson.substring(indexPerigo, indexPerigo + 4);
            boolean perigoso = perigoTexto.equals("true");

            int indexDist = textoJson.indexOf("\"kilometers\":\"") + 14;
            String distStr = textoJson.substring(indexDist, textoJson.indexOf("\"", indexDist));
            double distanciaKm = Double.parseDouble(distStr);
            
            // 4. Cria o nosso objeto! 
            Distancia distancia = new Distancia(distanciaKm * 1000.0);
            return new Asteroide(nome, distancia, perigoso);

        } catch (Exception e) {
            System.out.println("Falha ao buscar dados na internet: " + e.getMessage());
            return null;
        }
    }
}