package demo2;

import java.util.Scanner;

public class SistemaRastreador {
    public static void main(String[] args) {
        Scanner leitor = new Scanner(System.in);
        
        System.out.println("Iniciando o Rastreador Espacial...\n");
        
        // 1. Pergunta ao usuário
        System.out.println("Antes de iniciar, em qual unidade você deseja ver as distâncias?");
        System.out.println("1 - Quilômetros (km)");
        System.out.println("2 - Milhas (mi)");
        System.out.println("3 - Distâncias Lunares (LD)");
        System.out.print("Digite sua escolha (1/2/3): ");
        int escolha = leitor.nextInt();

        System.out.println("\nConectando aos servidores (NASA NeoWs, SBDB e CelesTrak)... aguarde.\n");
        
        // 2. Busca os dados nas APIs
        Asteroide asteroideReal = ClienteNasaNeoWs.buscarPrimeiroAsteroide();
        Satelite lixoEspacial = ClienteCelestrak.buscarEstacaoEspacial();
        Cometa halley = ClienteNasaSbdb.buscarCometaHalley();

        if (asteroideReal != null && lixoEspacial != null && halley != null) {
            
            // 3. Colocamos todos os filhos em um Array do tipo da classe pai (Herança)
            ObjetoEspacial[] todosObjetos = { asteroideReal, lixoEspacial, halley };

            // 4. Imprimimos o relatório formatado para CADA UM dos três objetos
            for (ObjetoEspacial obj : todosObjetos) {
                
                Distancia dist = obj.getDistancia();
                String distanciaFormatada = "";
                
                if (escolha == 1) {
                    distanciaFormatada = dist.emKilometros() + " km";
                } else if (escolha == 2) {
                    distanciaFormatada = dist.emMilhas() + " mi";
                } else if (escolha == 3) {
                    distanciaFormatada = dist.emDistanciasLunares() + " LD";
                } else {
                    distanciaFormatada = dist.emKilometros() + " km";
                }

                // Descobre o Nível de Risco
                NivelRisco risco = NivelRisco.BAIXO;
                if (obj instanceof Asteroide) {
                    risco = ((Asteroide) obj).avaliarRisco();
                } else if (obj instanceof Satelite) {
                    risco = ((Satelite) obj).avaliarRisco();
                } else if (obj instanceof Cometa) {
                    risco = ((Cometa) obj).avaliarRisco();
                }

                // Pega o tipo (ex: "Satélite") e deixa tudo maiúsculo para o título
                String tipoUpper = obj.getTipo().toUpperCase();

                // 5. Impressão adaptável exigida
                System.out.println("--- RELATÓRIO DO " + tipoUpper + " ESPACIAL DO DIA ---");
                System.out.println("Nome do Objeto: " + obj.getNome());
                System.out.println("Distancia do " + obj.getTipo() + ": " + distanciaFormatada);
                System.out.println("Nível de Risco: " + risco);
                System.out.println("--------------------------------------------");
            }
            
            // 6. Lógica para encontrar quem é o mais próximo
            ObjetoEspacial objetoMaisProximo = asteroideReal;

            if (lixoEspacial.getDistancia().emKilometros() < objetoMaisProximo.getDistancia().emKilometros()) {
                objetoMaisProximo = lixoEspacial;
            }
            if (halley.getDistancia().emKilometros() < objetoMaisProximo.getDistancia().emKilometros()) {
                objetoMaisProximo = halley;
            }
            
            // 7. Anúncio final
            System.out.println("\n=> RESUMO: O objeto mais próximo da Terra hoje é o " 
                               + objetoMaisProximo.getTipo() + " " + objetoMaisProximo.getNome() + "!");
            
        } else {
            System.out.println("Falha ao montar o relatório diário. Verifique sua conexão com a internet.");
        }
        
        leitor.close(); 
    }
}