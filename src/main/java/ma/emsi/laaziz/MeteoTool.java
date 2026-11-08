package ma.emsi.laaziz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class MeteoTool {

    // Seuil à partir duquel un parapluie est recommandé.
    private static final int SEUIL_PLUIE = 50;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Tool("Vérifie si de la pluie est prévue pendant les trois prochains jours "
            + "à partir de coordonnées géographiques.")
    public boolean isPluiePrevue(
            @P("Latitude géographique") double latitude,
            @P("Longitude géographique") double longitude) {

        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + latitude
                + "&longitude=" + longitude
                + "&daily=precipitation_probability_max"
                + "&forecast_days=3";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Erreur Open-Meteo : HTTP " + response.statusCode()
                );
            }

            JsonNode probabilites = mapper.readTree(response.body())
                    .path("daily")
                    .path("precipitation_probability_max");

            if (!probabilites.isArray() || probabilites.isEmpty()) {
                throw new IllegalStateException(
                        "Prévisions de pluie indisponibles."
                );
            }

            System.out.println("Probabilités de pluie :");

            boolean pluiePrevue = false;

            for (JsonNode valeur : probabilites) {
                if (!valeur.isNumber()) {
                    throw new IllegalStateException(
                            "Probabilité de pluie invalide."
                    );
                }

                int probabilite = valeur.asInt();
                System.out.println(probabilite + " %");

                if (probabilite >= SEUIL_PLUIE) {
                    pluiePrevue = true;
                }
            }

            return pluiePrevue;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de récupérer les prévisions météo.", e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Requête météo interrompue.", e
            );
        }
    }

    @Tool("Recherche la latitude et la longitude d'une ville à partir de son nom. "
            + "Utiliser cet outil avant isPluiePrevue si l'utilisateur donne une ville.")
    public double[] trouverCoordonnees(
            @P("Nom de la ville, par exemple Paris ou Rabat")
            String ville) {

        if (ville == null || ville.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom de la ville est vide."
            );
        }

        // Interroger l'API de géocodage Open-Meteo.
        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + URLEncoder.encode(
                ville.trim(), StandardCharsets.UTF_8
        )
                + "&count=1&language=en&format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Erreur géolocalisation : HTTP "
                                + response.statusCode()
                );
            }

            JsonNode resultats = mapper.readTree(response.body())
                    .path("results");

            if (!resultats.isArray() || resultats.isEmpty()) {
                throw new IllegalArgumentException(
                        "Ville introuvable : " + ville
                );
            }

            JsonNode premier = resultats.get(0);

            if (!premier.path("latitude").isNumber()
                    || !premier.path("longitude").isNumber()) {
                throw new IllegalStateException(
                        "Coordonnées géographiques invalides."
                );
            }

            double latitude = premier.path("latitude").asDouble();
            double longitude = premier.path("longitude").asDouble();

            System.out.println(
                    "Coordonnées de " + ville + " : "
                            + latitude + ", " + longitude
            );

            return new double[]{latitude, longitude};

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Erreur réseau de géolocalisation.", e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Géolocalisation interrompue.", e
            );
        }
    }
}