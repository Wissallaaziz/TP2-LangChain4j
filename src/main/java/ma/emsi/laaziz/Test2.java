package ma.emsi.laaziz;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.model.output.TokenUsage;

public class Test2 {

    // Tarifs standards de Gemini 3.5 Flash-Lite (USD / million de tokens).
    private static final double PRIX_ENTREE = 0.30;
    private static final double PRIX_SORTIE = 2.50;

    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Erreur : GEMINI_KEY est absente.");
            return;
        }

        // Modèle choisi pour correspondre aux tarifs utilisés.
        GoogleGenAiChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.7)
                .build();

        String question = "Quelle est la capitale du Maroc ?";

        // ChatResponse donne accès à la réponse et aux statistiques de tokens.
        ChatResponse response = model.chat(UserMessage.from(question));
        TokenUsage usage = response.tokenUsage();

        System.out.println("Question : " + question);
        System.out.println("Réponse : " + response.aiMessage().text());

        if (usage == null) {
            System.err.println("Statistiques de tokens indisponibles.");
            return;
        }

        int tokensEntree = usage.inputTokenCount();
        int tokensSortie = usage.outputTokenCount();

        // Coût théorique pour une requête au tarif payant standard.
        double cout = (tokensEntree * PRIX_ENTREE
                + tokensSortie * PRIX_SORTIE) / 1_000_000.0;

        System.out.println("Tokens en entrée : " + tokensEntree);
        System.out.println("Tokens en sortie : " + tokensSortie);
        System.out.println("Tokens au total : " + usage.totalTokenCount());
        System.out.printf("Coût estimé : %.8f USD%n", cout);

        if (cout > 0) {
            long nombreRequetes = (long) Math.ceil(1.0 / cout);
            System.out.println("Requêtes pour dépenser 1 USD : "
                    + nombreRequetes);
        }
    }
}

