package demo1;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class demo1 {

    public static void main(String[] args) {
        String apiKey = "DEMO_KEY"; // Substitua pela sua chave da NASA se tiver
        String hoje = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String urlString = "https://api.nasa.gov/neo/rest/v1/feed?start_date=" + hoje
                + "&end_date=" + hoje + "&detailed=false&api_key=" + apiKey;

        try {
            String response = fetchJson(urlString);
            String date = parseFirstDate(response);

            if (date == null) {
                System.out.println("Nenhum asteroide encontrado para hoje.");
                return;
            }

            String dayArray = extractDayArray(response, date);
            if (dayArray == null) {
                System.out.println("Nenhum asteroide encontrado para hoje.");
                return;
            }

            Asteroid best = findClosestAsteroid(dayArray);
            if (best == null) {
                System.out.println("Nenhum asteroide encontrado para hoje.");
                return;
            }

            System.out.println("🌌 Asteroide mais próximo hoje (" + date + "):");
            System.out.println("Nome: " + best.name);
            System.out.printf("Distância: %,.2f km%n", best.distanceKm);
            System.out.println("É perigoso? " + (best.hazardous ? "Sim" : "Não"));

        } catch (Exception e) {
            System.err.println("Erro ao comunicar com a API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String fetchJson(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        int status = connection.getResponseCode();
        BufferedReader reader;

        if (status == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
            StringBuilder errorBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                errorBody.append(line).append('\n');
            }
            reader.close();
            throw new IllegalStateException("Erro na requisição. Código HTTP: " + status + "\n" + errorBody.toString());
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private static String parseFirstDate(String json) {
        Pattern pattern = Pattern.compile("\\\"(\\d{4}-\\d{2}-\\d{2})\\\"\\s*:\\s*\\[");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String extractDayArray(String json, String date) {
        String search = "\"" + date + "\"";
        int pos = json.indexOf(search);
        if (pos < 0) {
            return null;
        }

        int bracketStart = json.indexOf('[', pos);
        if (bracketStart < 0) {
            return null;
        }

        int depth = 0;
        for (int i = bracketStart; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return json.substring(bracketStart, i + 1);
                }
            }
        }

        return null;
    }

    private static Asteroid findClosestAsteroid(String arrayJson) {
        int index = 0;
        Asteroid best = null;
        while (index < arrayJson.length()) {
            index = arrayJson.indexOf('{', index);
            if (index < 0) {
                break;
            }

            int depth = 0;
            int start = index;
            for (; index < arrayJson.length(); index++) {
                char c = arrayJson.charAt(index);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        String asteroidJson = arrayJson.substring(start, index + 1);
                        Asteroid current = parseAsteroid(asteroidJson);
                        if (current != null && (best == null || current.distanceKm < best.distanceKm)) {
                            best = current;
                        }
                        index++;
                        break;
                    }
                }
            }
        }
        return best;
    }

    private static Asteroid parseAsteroid(String json) {
        String name = extractStringValue(json, "name");
        String hazardText = extractLiteralValue(json, "is_potentially_hazardous_asteroid");
        String kilometers = extractStringValue(json, "kilometers");

        if (name == null || hazardText == null || kilometers == null) {
            return null;
        }

        boolean hazardous = "true".equalsIgnoreCase(hazardText);
        double distanceKm;
        try {
            distanceKm = Double.parseDouble(kilometers);
        } catch (NumberFormatException e) {
            return null;
        }

        return new Asteroid(name, hazardous, distanceKm);
    }

    private static String extractStringValue(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"(.*?)\\\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String extractLiteralValue(String json, String key) {
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static class Asteroid {
        final String name;
        final boolean hazardous;
        final double distanceKm;

        Asteroid(String name, boolean hazardous, double distanceKm) {
            this.name = name;
            this.hazardous = hazardous;
            this.distanceKm = distanceKm;
        }
    }
}
