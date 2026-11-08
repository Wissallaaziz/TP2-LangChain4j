package ma.emsi.laaziz;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
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

public class Test5 {

    public static void main(String[] args) {

        String apiKey = System.getenv("GEMINI_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Erreur : GEMINI_KEY est absente.");
            return;
        }

        Path fichier = Path.of("infos.txt");

        if (!Files.isRegularFile(fichier)) {
            System.err.println("Erreur : infos.txt introuvable.");
            return;
        }

        // Charger le document fourni au RAG.
        Document document = FileSystemDocumentLoader.loadDocument(
                fichier.toAbsolutePath()
        );

        // Sélection explicite du modèle pour éviter le conflit Maven.
        EmbeddingModel embeddingModel = new BgeSmallEnV15EmbeddingModel();

        // Stockage vectoriel temporaire en mémoire.
        InMemoryEmbeddingStore<TextSegment> store =
                new InMemoryEmbeddingStore<>();

        // Indexer le document avec le modèle sélectionné.
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build();

        ingestor.ingest(document);

        // Utiliser le même modèle pour rechercher les passages pertinents.
        ContentRetriever retriever =
                EmbeddingStoreContentRetriever.builder()
                        .embeddingModel(embeddingModel)
                        .embeddingStore(store)
                        .build();

        // Initialiser Gemini.
        GoogleGenAiChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.7)
                .build();

        // Construire l'assistant avec mémoire et RAG.
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .contentRetriever(retriever)
                .build();

        String question = "Comment s'appelle le chat de Pierre ?";

        System.out.println("=== TEST 5 : AVEC RAG ===");
        System.out.println("Question : " + question);
        System.out.println("Réponse : " + assistant.chat(question));
    }
}