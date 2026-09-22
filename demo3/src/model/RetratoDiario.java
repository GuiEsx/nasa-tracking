package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RetratoDiario {
    private final LocalDate data;
    private final Set<ObjetoEspacial> objetos;

    public RetratoDiario(LocalDate data) {
        this.data = data != null ? data : LocalDate.now();
        this.objetos = new LinkedHashSet<>();
    }

    public RetratoDiario(LocalDate data, Collection<? extends ObjetoEspacial> colecaoInicial) {
        this(data);
        if (colecaoInicial != null) {
            this.objetos.addAll(colecaoInicial);
        }
    }

    // RF9 [P]: Adiciona ao conjunto garantindo desduplicação
    public boolean adicionarObjeto(ObjetoEspacial objeto) {
        if (objeto == null) {
            return false;
        }
        return this.objetos.add(objeto);
    }

    public void adicionarTodos(Collection<? extends ObjetoEspacial> novosObjetos) {
        if (novosObjetos != null) {
            for (ObjetoEspacial obj : novosObjetos) {
                adicionarObjeto(obj);
            }
        }
    }

    public LocalDate getData() {
        return data;
    }

    public Set<ObjetoEspacial> getObjetos() {
        return Collections.unmodifiableSet(this.objetos);
    }

    // RF7 [C]: Respostas a perguntas agregadas sobre a coleção do dia
    public int totalObjetos() {
        return this.objetos.size();
    }

    public ObjetoEspacial obterMaisProximo() {
        if (objetos.isEmpty()) {
            return null;
        }

        ObjetoEspacial maisProximo = null;
        for (ObjetoEspacial obj : objetos) {
            if (maisProximo == null || obj.getDistancia().emKilometros() < maisProximo.getDistancia().emKilometros()) {
                maisProximo = obj;
            }
        }
        return maisProximo;
    }

    public ObjetoEspacial obterMaisPerigoso() {
        if (objetos.isEmpty()) {
            return null;
        }

        ObjetoEspacial maisPerigoso = null;
        for (ObjetoEspacial obj : objetos) {
            if (maisPerigoso == null || obj.avaliarRisco().isMaisPerigosoQue(maisPerigoso.avaliarRisco())) {
                maisPerigoso = obj;
            }
        }
        return maisPerigoso;
    }

    public Map<NivelRisco, Integer> contagemPorRisco() {
        Map<NivelRisco, Integer> mapa = new EnumMap<>(NivelRisco.class);
        for (NivelRisco r : NivelRisco.values()) {
            mapa.put(r, 0);
        }
        for (ObjetoEspacial obj : objetos) {
            NivelRisco risco = obj.avaliarRisco();
            mapa.put(risco, mapa.get(risco) + 1);
        }
        return mapa;
    }

    public List<ObjetoEspacial> filtrarPorRisco(NivelRisco riscoBuscado) {
        List<ObjetoEspacial> resultado = new ArrayList<>();
        for (ObjetoEspacial obj : objetos) {
            if (obj.avaliarRisco() == riscoBuscado) {
                resultado.add(obj);
            }
        }
        return resultado;
    }

    public List<ObjetoEspacial> filtrarPorTipo(String tipoBuscado) {
        List<ObjetoEspacial> resultado = new ArrayList<>();
        for (ObjetoEspacial obj : objetos) {
            if (obj.getTipo().equalsIgnoreCase(tipoBuscado)) {
                resultado.add(obj);
            }
        }
        return resultado;
    }

    public double calcularDistanciaMediaKm() {
        if (objetos.isEmpty()) {
            return 0.0;
        }
        double soma = 0.0;
        for (ObjetoEspacial obj : objetos) {
            soma += obj.getDistancia().emKilometros();
        }
        return soma / objetos.size();
    }

    // RF7 [C]: Formato exato: "Retrato de DD/MM/AAAA: X objetos. Mais próximo: [Tipo] [Nome] a Y distâncias lunares. Mais perigoso: [Nome] (RISCO)."
    public String gerarResumoFormatado() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dataStr = data.format(dtf);

        if (objetos.isEmpty()) {
            return String.format("Retrato de %s: 0 objetos rastreados.", dataStr);
        }

        ObjetoEspacial maisProximo = obterMaisProximo();
        ObjetoEspacial maisPerigoso = obterMaisPerigoso();

        String strMaisProximo = (maisProximo != null)
                ? String.format("%s %s a %.1f distâncias lunares (%.2f km)",
                maisProximo.getTipo().toLowerCase(),
                maisProximo.getNome(),
                maisProximo.getDistancia().emDistanciasLunares(),
                maisProximo.getDistancia().emKilometros())
                : "Nenhum";

        String strMaisPerigoso = (maisPerigoso != null)
                ? String.format("%s (%s)", maisPerigoso.getNome(), maisPerigoso.avaliarRisco())
                : "Nenhum";

        return String.format("Retrato de %s: %d objetos. Mais próximo: %s. Mais perigoso: %s.",
                dataStr, totalObjetos(), strMaisProximo, strMaisPerigoso);
    }
}
