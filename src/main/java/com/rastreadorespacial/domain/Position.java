package com.rastreadorespacial.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Representa a posição espacial de um objeto em um determinado instante de tempo.
 *
 * <p><strong>Interpretação das coordenadas:</strong></p>
 * <ul>
 *   <li>Para satélites em órbita terrestre, {@code latitudeGraus} e {@code longitudeGraus} representam
 *   a posição sub-satélite (sub-terrestre) projetada na superfície da Terra, e {@code altitude} representa a altitude orbital sobre a superfície.</li>
 *   <li>Para asteroides e cometas no espaço profundo, {@code latitudeGraus} e {@code longitudeGraus} representam
 *   coordenadas celestes aparentes aproximadas (declinação e ascensão reta em graus), e {@code altitude} representa a distância heliocêntrica ou geocêntrica estimada.</li>
 * </ul>
 *
 * @param latitudeGraus latitude geográfica ou declinação celeste aproximada (-90.0 a +90.0)
 * @param longitudeGraus longitude geográfica ou ascensão reta aproximada (-180.0 a +180.0)
 * @param altitude distância/altitude do objeto no espaço (utilizando {@link Distance})
 * @param instanteDaMedicao timestamp do instante da medição/estimativa
 */
public record Position(
        double latitudeGraus,
        double longitudeGraus,
        Distance altitude,
        Instant instanteDaMedicao
) {
    public Position {
        Objects.requireNonNull(altitude, "A altitude/distância não pode ser nula.");
        Objects.requireNonNull(instanteDaMedicao, "O instante da medição não pode ser nulo.");

        if (latitudeGraus < -90.0 || latitudeGraus > 90.0) {
            throw new IllegalArgumentException(String.format("Latitude inválida (deve estar entre -90 e +90): %.4f", latitudeGraus));
        }
        if (longitudeGraus < -180.0 || longitudeGraus > 180.0) {
            throw new IllegalArgumentException(String.format("Longitude inválida (deve estar entre -180 e +180): %.4f", longitudeGraus));
        }
    }
}

