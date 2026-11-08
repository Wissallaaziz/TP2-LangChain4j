package ma.emsi.laaziz;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.util.Scanner;

public class Test7Outils {

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Erreur : GEMINI_KEY est absente.");
            return;
        }

        // Créer Gemini et activer les logs des requêtes et réponses.
        GoogleGenAiChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.7)
                .logRequestsAndResponses(true)
                .build();

        // Rendre les deux outils météo accessibles à Gemini.
        AssistantMeteo assistant = AiServices.builder(AssistantMeteo.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .tools(new MeteoTool())
                .build();

        System.out.println("=== TEST 7 : GEMINI AVEC DEUX OUTILS METEO ===");
        System.out.println("Logs des requêtes et réponses activés.");
        System.out.println("Outils disponibles :");
        System.out.println("- Prévisions de pluie");
        System.out.println("- Géolocalisation d'une ville");
        System.out.println("Écris 'quit' pour terminer.");

        // Conserver l'historique des questions pendant la session.
        try (Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.print("\nQuestion : ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                String question = scanner.nextLine().trim();

                if (question.equalsIgnoreCase("quit")) {
                    break;
                }

                if (question.isBlank()) {
                    continue;
                }

                try {
                    String reponse = assistant.chat(question);
                    System.out.println("Réponse : " + reponse);

                } catch (RuntimeException e) {
                    System.err.println(
                            "Erreur Gemini : " + e.getMessage()
                    );
                }
            }
        }

        System.out.println("Fin de la conversation.");
    }
}