package ma.emsi.laaziz;

import java.util.Map;

import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

public class Test3 {

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Erreur : GEMINI_KEY est absente.");
            return;
        }

        // Création du modèle Gemini.
        GoogleGenAiChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.7)
                .build();

        // Test 1 : traduction avec un seul paramètre.
        PromptTemplate template1 = PromptTemplate.from(
                "Traduis le texte suivant en anglais : {{texte}}"
        );

        Prompt prompt1 = template1.apply(Map.of(
                "texte", "Bonjour, comment allez-vous aujourd'hui ?"
        ));

        System.out.println("=== Traduction 1 : Français vers Anglais ===");
        System.out.println("Prompt : " + prompt1.text());
        System.out.println("Réponse : " + model.chat(prompt1.text()));

        // Test 2 : traduction avec deux paramètres.
        PromptTemplate template2 = PromptTemplate.from(
                "Traduis le texte suivant en {{langue}} : {{texte}}"
        );

        Prompt prompt2 = template2.apply(Map.of(
                "langue", "espagnol",
                "texte", "Je suis étudiante en informatique."
        ));

        System.out.println("\n=== Traduction 2 : Français vers Espagnol ===");
        System.out.println("Prompt : " + prompt2.text());
        System.out.println("Réponse : " + model.chat(prompt2.text()));
    }
}