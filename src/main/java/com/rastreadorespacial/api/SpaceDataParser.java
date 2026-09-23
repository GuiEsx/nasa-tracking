package com.rastreadorespacial.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rastreadorespacial.domain.Asteroid;
import com.rastreadorespacial.domain.Distance;
import com.rastreadorespacial.domain.Satellite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utilitário para conversão das respostas JSON brutas das APIs em entidades de domínio ricas.
 */
public final class SpaceDataParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SpaceDataParser() {
        // Classe utilitária estática
    }

    /**
     * Realiza o parsing da resposta JSON da API NASA NeoWs para uma lista de objetos {@link Asteroid}.
     *
     * @param rawJson JSON bruto do feed NeoWs
     * @return lista de asteroides encontrados
     */
    public static List<Asteroid> parseNeoWs(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }

        List<Asteroid> asteroides = new ArrayList<>();
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(rawJson);
            JsonNode nearEarthObjectsNode = rootNode.path("near_earth_objects");

            if (nearEarthObjectsNode.isObject()) {
                Iterator<JsonNode> dateEntries = nearEarthObjectsNode.elements();
                while (dateEntries.hasNext()) {
                    JsonNode dateArray = dateEntries.next();
                    if (dateArray.isArray()) {
                        for (JsonNode neo : dateArray) {
                            String id = neo.path("id").asText(neo.path("neo_reference_id").asText(""));
                            String name = neo.path("name").asText("Asteroide Desconhecido");
                            boolean isHazardous = neo.path("is_potentially_hazardous_asteroid").asBoolean(false);

                            Distance distance = Distance.ofLunarDistances(50.0); // valor padrão seguro
                            JsonNode closeApproachArray = neo.path("close_approach_data");
                            if (closeApproachArray.isArray() && !closeApproachArray.isEmpty()) {
                                JsonNode firstApproach = closeApproachArray.get(0);
                                JsonNode missDistanceNode = firstApproach.path("miss_distance");

                                if (missDistanceNode.hasNonNull("lunar")) {
                                    double ld = missDistanceNode.path("lunar").asDouble(50.0);
                                    distance = Distance.ofLunarDistances(ld);
                                } else if (missDistanceNode.hasNonNull("kilometers")) {
                                    double km = missDistanceNode.path("kilometers").asDouble(19_220_000.0);
                                    distance = Distance.ofKilometers(km);
                                }
                            }

                            if (!id.isBlank()) {
                                asteroides.add(new Asteroid(id, name, distance, isHazardous));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Aviso: Falha ao processar JSON da NASA NeoWs: " + e.getMessage());
        }

        return asteroides;
    }

    /**
     * Realiza o parsing da resposta JSON da API Celestrak para uma lista de objetos {@link Satellite}.
     *
     * @param rawJson JSON bruto do Celestrak
     * @return lista de satélites encontrados
     */
    public static List<Satellite> parseCelestrak(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }

        List<Satellite> satelites = new ArrayList<>();
        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(rawJson);

            if (rootNode.isArray()) {
                for (JsonNode satNode : rootNode) {
                    String id = satNode.path("NORAD_CAT_ID").asText(satNode.path("OBJECT_ID").asText(""));
                    String name = satNode.path("OBJECT_NAME").asText("Satélite Desconhecido");

                    Double bstar = satNode.hasNonNull("BSTAR") ? satNode.path("BSTAR").asDouble() : null;
                    Double inclination = satNode.hasNonNull("INCLINATION") ? satNode.path("INCLINATION").asDouble() : null;
                    Double eccentricity = satNode.hasNonNull("ECCENTRICITY") ? satNode.path("ECCENTRICITY").asDouble() : null;
                    Double meanMotion = satNode.hasNonNull("MEAN_MOTION") ? satNode.path("MEAN_MOTION").asDouble() : null;

                    if (!id.isBlank()) {
                        satelites.add(new Satellite(id, name, bstar, inclination, eccentricity, meanMotion));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Aviso: Falha ao processar JSON do Celestrak: " + e.getMessage());
        }

        return satelites;
    }
}

