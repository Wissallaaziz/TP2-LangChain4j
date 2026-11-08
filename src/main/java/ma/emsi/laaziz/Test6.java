package ma.emsi.laaziz;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallenv15.BgeSmallEnV15EmbeddingModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Test6 {

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Erreur : GEMINI_KEY est absente.");
            return;
        }

        Path fichier = Path.of("langchain4j.pdf");

        if (!Files.isRegularFile(fichier)) {
            System.err.println("Erreur : langchain4j.pdf introuvable.");
            return;
        }

        System.out.println("=== TEST 6 : RAG AVEC PDF ===");
        System.out.println("Chargement du PDF...");

        // Apache Tika extrait le texte contenu dans le PDF.
        Document document = FileSystemDocumentLoader.loadDocument(
                fichier.toAbsolutePath(),
                new ApacheTikaDocumentParser()
        );

        if (document.text().isBlank()) {
            System.err.println("Erreur : aucun texte extrait du PDF.");
            return;
        }

        // Réutilisation du modèle local compatible avec notre Mac Intel.
        EmbeddingModel embeddingModel = new BgeSmallEnV15EmbeddingModel();

        // Les segments du PDF sont stockés en mémoire.
        InMemoryEmbeddingStore<TextSegment> store =
                new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build();

        System.out.println("Indexation du PDF...");
        ingestor.ingest(document);

        // Les passages pertinents seront retrouvés pour chaque question.
        ContentRetriever retriever =
                EmbeddingStoreContentRetriever.builder()
                        .embeddingModel(embeddingModel)
                        .embeddingStore(store)
                        .maxResults(5)
                        .build();

        GoogleGenAiChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.7)
                .build();

        // La mémoire permet de conserver les échanges entre les questions.
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .contentRetriever(retriever)
                .build();

        System.out.println("PDF prêt !");
        System.out.println("Pose une question ou écris 'quit' pour terminer.");

        // Plusieurs questions sont possibles sans relancer le programme.
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
                    System.err.println("Erreur Gemini : " + e.getMessage());
                }
            }
        }

        System.out.println("Fin de la conversation.");
    }
}