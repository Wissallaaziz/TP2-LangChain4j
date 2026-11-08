package ma.emsi.laaziz;

import java.time.Duration;
import java.util.Locale;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.model.google.genai.GoogleGenAiEmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;

public class Test4 {

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Erreur : GEMINI_KEY est absente.");
            return;
        }

        // Modèle local : aucune consommation de quota Gemini.
        EmbeddingModel modeleLocal = new BgeSmallEnV15EmbeddingModel();

        // Modèle Google adapté aux comparaisons sémantiques.
        EmbeddingModel modeleGoogle = GoogleGenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-embedding-001")
                .taskType(
                        GoogleGenAiEmbeddingModel.TaskTypeEnum.SEMANTIC_SIMILARITY
                )
                .timeout(Duration.ofSeconds(30))
                .build();

        // Six couples initiaux et deux couples supplémentaires en français.
        String[][] phrases = {
                {"The cat is sleeping.", "A kitten is resting."},
                {"I love sunny weather.", "I hate sunny weather."},
                {"The car is very fast.", "The vehicle moves quickly."},
                {"I am learning Java.", "I am studying programming."},
                {"The sky is blue.", "I enjoy eating pizza."},
                {"She is happy today.", "She feels sad today."},
                {"Le chat dort sur le canapé.", "Un félin se repose sur le sofa."},
                {"Je prépare un examen de Java.", "J'étudie pour une évaluation en programmation Java."}
        };

        System.out.println("=== TEST 4 : COMPARAISON DES EMBEDDINGS ===");

        for (int i = 0; i < phrases.length; i++) {

            String phrase1 = phrases[i][0];
            String phrase2 = phrases[i][1];

            // Similarité calculée localement.
            Embedding local1 = modeleLocal.embed(phrase1).content();
            Embedding local2 = modeleLocal.embed(phrase2).content();

            double similariteLocale = CosineSimilarity.between(
                    local1, local2
            );

            System.out.println("\nCouple " + (i + 1));
            System.out.println("Phrase 1 : " + phrase1);
            System.out.println("Phrase 2 : " + phrase2);

            System.out.printf(
                    Locale.US,
                    "BGE Small : %.4f%n",
                    similariteLocale
            );

            // Une erreur de quota Google ne bloque pas les résultats locaux.
            try {
                Embedding google1 = modeleGoogle.embed(phrase1).content();
                Embedding google2 = modeleGoogle.embed(phrase2).content();

                double similariteGoogle = CosineSimilarity.between(
                        google1, google2
                );

                System.out.printf(
                        Locale.US,
                        "Google Gemini : %.4f%n",
                        similariteGoogle
                );

            } catch (RuntimeException e) {
                System.err.println(
                        "Erreur Google Gemini : " + e.getMessage()
                );

                // Évite de multiplier les requêtes si le quota est épuisé.
                break;
            }
        }
    }
}