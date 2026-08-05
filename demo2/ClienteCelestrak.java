package demo2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClienteCelestrak {

    public static Satelite buscarEstacaoEspacial() {
        try {
            // 1. URL pública do CelesTrak buscando a ISS (ID 25544)
            String urlSatelite = "https://celestrak.org/NORAD/elements/gp.php?CATNR=25544&FORMAT=json";
            URL url = new URL(urlSatelite);
            
            // 2. Faz a conexão (Padrão Java 8)
            HttpURLConnection conexao = (HttpURLConnection) url.openConnection();
            conexao.setRequestMethod("GET");
            
            // Disfarça o nosso código como um navegador (algumas APIs bloqueiam robôs)
            conexao.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            BufferedReader leitor = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            StringBuilder resposta = new StringBuilder();
            String linha;
            while ((linha = leitor.readLine()) != null) {
                resposta.append(linha);
            }
            leitor.close();
            
            String textoJson = resposta.toString();

            // 3. Recorta os dados do Satélite
            int indexNome = textoJson.indexOf("\"OBJECT_NAME\":\"") + 15;
            String nome = textoJson.substring(indexNome, textoJson.indexOf("\"", indexNome));

            // Decaimento e altitude são dados matemáticos complexos no JSON (Mean Motion).
            // Para o escopo de POO, nós extraímos o nome real da API e simulamos a distância
            Distancia altitudeAtual = new Distancia(420000.0); // ISS fica a ~420 km (em metros)
            double decaimentoSimulado = 50.0; // Cai 50 metros por dia

            // 4. Cria e retorna o objeto Satelite
            return new Satelite(nome, altitudeAtual, decaimentoSimulado);

        } catch (Exception e) {
            System.out.println("Falha ao buscar o Satélite na API: " + e.getMessage());
            return null;
        }
    }
}