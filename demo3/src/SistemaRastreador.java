import model.Asteroide;
import model.Cometa;
import model.Satelite;
import service.ClienteCelestrak;
import service.ClienteNasaNeoWs;
import service.ClienteNasaSbdb;
import service.RastreadorService;
import view.MenuConsole;

import java.util.Scanner;

public class SistemaRastreador {

    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in);

        System.out.println("Iniciando o Rastreador Espacial...");
        System.out.println("Conectando aos servidores (NASA NeoWs, SBDB e CelesTrak)... aguarde.\n");

        // Busca de dados prévia
        Asteroide asteroideReal = ClienteNasaNeoWs.buscarPrimeiroAsteroide();
        Satelite lixoEspacial = ClienteCelestrak.buscarEstacaoEspacial();
        Cometa halley = ClienteNasaSbdb.buscarCometaHalley();

        if (asteroideReal == null || lixoEspacial == null || halley == null) {
            System.out.println("Aviso: falha ao obter dados das APIs. Usando objetos de fallback para manter o sistema funcionando.");
            if (asteroideReal == null) {
                asteroideReal = RastreadorService.criarAsteroideFallback();
            }
            if (lixoEspacial == null) {
                lixoEspacial = RastreadorService.criarSateliteFallback();
            }
            if (halley == null) {
                halley = RastreadorService.criarCometaFallback();
            }
        }

        // Inicia o menu interativo
        MenuConsole.executarMenuPrincipal(leitor, asteroideReal, lixoEspacial, halley);

        leitor.close();
    }
}

