package model;

import java.time.LocalDate;

public class ApodRegistro {
    private final String titulo;
    private final String explicacao;
    private final String urlMidia;
    private final LocalDate data;

    public ApodRegistro(String titulo, String explicacao, String urlMidia, LocalDate data) {
        this.titulo = titulo;
        this.explicacao = explicacao;
        this.urlMidia = urlMidia;
        this.data = data;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public String getUrlMidia() {
        return urlMidia;
    }

    public LocalDate getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("APOD [%s]: %s (URL: %s)", data, titulo, urlMidia);
    }
}

