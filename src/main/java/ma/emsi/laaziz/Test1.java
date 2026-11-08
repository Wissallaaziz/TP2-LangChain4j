package ma.emsi.laaziz;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;

public class Test1 {
    public static void main(String[] args) {

        ChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .modelName("gemini-3.5-flash")
                .temperature(0.7)
                .build();

        // Question simple
        System.out.println("=== QUESTION 1 ===");
        String reponse1 = model.chat("Bonjour ! Explique-moi simplement ce qu'est LangChain4j.");
        System.out.println(reponse1);

        // Question sur l'heure actuelle
        System.out.println("\n=== QUESTION 2 ===");
        String reponse2 = model.chat("Quelle heure est-il actuellement ?");
        System.out.println(reponse2);

        // Première question avec un nom
        System.out.println("\n=== QUESTION 3 ===");
        String reponse3 = model.chat("Bonjour, je m'appelle Wissal. Quel est mon prénom ?");
        System.out.println(reponse3);

        // Deuxième question : vérifier la mémoire
        System.out.println("\n=== QUESTION 4 ===");
        String reponse4 = model.chat("Quel prénom viens-je de te donner ?");
        System.out.println(reponse4);
    }
}
