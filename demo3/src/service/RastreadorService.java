package service;

import model.Asteroide;
import model.Cometa;
import model.Distancia;
import model.ObjetoEspacial;
import model.ObjetoRastreado;
import model.PosicaoAtual;
import model.RetratoDiario;
import model.Satelite;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class RastreadorService {

    public static Asteroide criarAsteroideFallback() {
        return new Asteroide("2014-OL339", "2014 OL339", Distancia.deKilometros(15000000.0), false, LocalDateTime.now());
    }

    public static Satelite criarSateliteFallback() {
        return new Satelite("25544", "ISS (Estação Espacial)", Distancia.deKilometros(420.0), 45.0, LocalDateTime.now());
    }

    public static Cometa criarCometaFallback() {
        return new Cometa("1P", "1P/Halley", Distancia.deDistanciasLunares(2.0), 11.0, LocalDateTime.now());
    }

    public static LocalDateTime obterUltimoAvistamento(ObjetoEspacial objeto, LocalDateTime agora) {
        if (objeto != null && objeto.getUltimoAvistamento() != null) {
            return objeto.getUltimoAvistamento();
        }
        return agora != null ? agora.minusMinutes(45) : LocalDateTime.now();
    }

    // RF6: Delegação direta para o método da interface Posicionavel do objeto
    public static PosicaoAtual calcularPosicaoAtual(ObjetoEspacial objeto, LocalDateTime referencia, LocalDateTime consulta) {
        if (objeto != null) {
            return objeto.consultarPosicao(consulta);
        }
        return new PosicaoAtual("Posição desconhecida", Distancia.deMetros(0), referencia, consulta);
    }

    public static ObjetoEspacial obterObjetoMaisProximoGlobal(Asteroide asteroide, Satelite satelite, Cometa cometa) {
        ObjetoEspacial maisProximo = asteroide;

        if (satelite != null && (maisProximo == null || satelite.getDistancia().emKilometros() < maisProximo.getDistancia().emKilometros())) {
            maisProximo = satelite;
        }
        if (cometa != null && (maisProximo == null || cometa.getDistancia().emKilometros() < maisProximo.getDistancia().emKilometros())) {
            maisProximo = cometa;
        }

        return maisProximo;
    }

    // RF5 [P]: Classificação unificada sem lógica fixa por tipo (CRÍTICO -> BAIXO)
    public static void ordenarPorRisco(List<ObjetoRastreado> objetos) {
        if (objetos != null) {
            objetos.sort((o1, o2) -> o2.getRisco().compareTo(o1.getRisco()));
        }
    }

    // RF5 [P]: Ordenação de coleção mista de ObjetoEspacial usando polimorfismo puro (sem qualquer 'if instanceof')
    public static void ordenarObjetosPorRisco(List<? extends ObjetoEspacial> objetos) {
        if (objetos != null) {
            objetos.sort(ObjetoEspacial.POR_RISCO_DECRESCENTE);
        }
    }

    // RF5 [P]: Demonstração canônica da especificação (2 asteroides, 1 cometa e 1 satélite)
    public static List<ObjetoEspacial> criarColecaoMistaDemonstracao(Asteroide asteroideReal, Satelite lixoEspacial, Cometa halley) {
        Asteroide a1 = (asteroideReal != null) ? asteroideReal : criarAsteroideFallback();
        // Segundo asteroide com características críticas para testar ordenação mista
        Asteroide a2Critico = new Asteroide("99942", "99942 Apophis (Crítico)", Distancia.deDistanciasLunares(0.08), true, LocalDateTime.now());
        Satelite s1 = (lixoEspacial != null) ? lixoEspacial : criarSateliteFallback();
        Cometa c1 = (halley != null) ? halley : criarCometaFallback();

        List<ObjetoEspacial> colecao = new java.util.ArrayList<>();
        colecao.add(a1);         // Asteroide 1
        colecao.add(s1);         // Satélite 1
        colecao.add(c1);         // Cometa 1
        colecao.add(a2Critico);  // Asteroide 2 (Crítico)
        return colecao;
    }

    public static RetratoDiario criarRetratoDiario(LocalDate data, ObjetoEspacial... objetos) {
        RetratoDiario retrato = new RetratoDiario(data != null ? data : LocalDate.now());
        if (objetos != null) {
            retrato.adicionarTodos(Arrays.asList(objetos));
        }
        return retrato;
    }
}
