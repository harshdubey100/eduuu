package com.eduverse.backend.service;

import com.eduverse.backend.Entity.ContentChunk;
import com.eduverse.backend.Entity.Course;
import com.eduverse.backend.Entity.Lesson;
import com.eduverse.backend.dto.RagAnswerResponseDTO;
import com.eduverse.backend.exception.CourseNotFoundException;
import com.eduverse.backend.repository.ContentChunkRepository;
import com.eduverse.backend.repository.CourseRepository;
import com.eduverse.backend.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Lightweight, dependency-free Retrieval-Augmented-Generation support for a course.
 * <p>
 * This implements the "retrieval" half of RAG using classic TF-IDF + cosine
 * similarity over locally computed vectors - it deliberately does NOT call an
 * external embeddings API, so the whole pipeline runs offline with no API key.
 * <p>
 * In a production build, {@link #vectorize} would be swapped for a real
 * embedding model call (e.g. OpenAI/Cohere embeddings or a local
 * sentence-transformers model), and a generation step (an LLM call fed the
 * top-k chunks as context) would be layered on top of the results this class
 * returns. The chunking, storage and ranking architecture stays the same.
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private static final int CHUNK_SIZE_CHARS = 400;
    private static final int TOP_K = 3;

    private static final Set<String> STOPWORDS = Set.of(
            "the", "is", "at", "of", "a", "an", "in", "on", "to", "and", "or",
            "for", "with", "this", "that", "it", "as", "are", "be", "by", "from",
            "will", "can", "you", "your", "we", "our", "how", "what"
    );

    private final ContentChunkRepository contentChunkRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public RagService(ContentChunkRepository contentChunkRepository,
                       CourseRepository courseRepository,
                       LessonRepository lessonRepository) {
        this.contentChunkRepository = contentChunkRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }

    // Splits every lesson in the course into small text chunks and stores them.
    // Called whenever a lesson is added/updated so the index stays fresh.
    @Transactional
    public void reindexCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Course with ID " + courseId + " not found"));

        List<Lesson> lessons = lessonRepository.findByModuleCourseId(courseId);

        contentChunkRepository.deleteByCourseId(courseId);

        List<ContentChunk> newChunks = new ArrayList<>();
        for (Lesson lesson : lessons) {
            if (lesson.getContent() == null || lesson.getContent().isBlank()) {
                continue;
            }
            for (String piece : splitIntoChunks(lesson.getContent())) {
                ContentChunk chunk = new ContentChunk();
                chunk.setCourse(course);
                chunk.setLesson(lesson);
                chunk.setChunkText(piece);
                chunk.setVector(""); // computed lazily/live at query time - see class javadoc
                newChunks.add(chunk);
            }
        }

        contentChunkRepository.saveAll(newChunks);
        log.info("action=rag_reindex courseId={} chunkCount={}", courseId, newChunks.size());
    }

    // Returns the most relevant chunks of course content for a natural-language question
    public RagAnswerResponseDTO search(Long courseId, String question) {
        List<ContentChunk> chunks = contentChunkRepository.findByCourseId(courseId);

        List<List<String>> tokenizedChunks = new ArrayList<>();
        for (ContentChunk chunk : chunks) {
            tokenizedChunks.add(tokenize(chunk.getChunkText()));
        }

        List<String> vocabulary = buildVocabulary(tokenizedChunks);
        Map<String, Double> idf = computeIdf(vocabulary, tokenizedChunks);

        List<double[]> chunkVectors = new ArrayList<>();
        for (List<String> tokens : tokenizedChunks) {
            chunkVectors.add(vectorize(tokens, vocabulary, idf));
        }

        double[] queryVector = vectorize(tokenize(question), vocabulary, idf);

        List<RagAnswerResponseDTO.RetrievedChunkDTO> ranked = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            double score = cosineSimilarity(queryVector, chunkVectors.get(i));
            ranked.add(new RagAnswerResponseDTO.RetrievedChunkDTO(
                    chunks.get(i).getLesson().getTitle(),
                    excerpt(chunks.get(i).getChunkText()),
                    Math.round(score * 1000.0) / 1000.0
            ));
        }

        ranked.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));
        List<RagAnswerResponseDTO.RetrievedChunkDTO> topMatches = ranked.subList(0, Math.min(TOP_K, ranked.size()));

        return new RagAnswerResponseDTO(question, new ArrayList<>(topMatches));
    }

    // --- chunking -----------------------------------------------------

    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        String[] words = content.trim().split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() + 1 > CHUNK_SIZE_CHARS && current.length() > 0) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
            current.append(word).append(" ");
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    private String excerpt(String text) {
        return text.length() <= 220 ? text : text.substring(0, 220).trim() + "...";
    }

    // --- TF-IDF vector math --------------------------------------------

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        for (String raw : text.toLowerCase().split("[^a-z0-9]+")) {
            if (raw.length() > 1 && !STOPWORDS.contains(raw)) {
                tokens.add(raw);
            }
        }
        return tokens;
    }

    private List<String> buildVocabulary(List<List<String>> documents) {
        TreeSet<String> vocab = new TreeSet<>();
        for (List<String> doc : documents) {
            vocab.addAll(doc);
        }
        return new ArrayList<>(vocab);
    }

    private Map<String, Double> computeIdf(List<String> vocabulary, List<List<String>> documents) {
        Map<String, Double> idf = new HashMap<>();
        int totalDocs = documents.size();

        for (String term : vocabulary) {
            int docFrequency = 0;
            for (List<String> doc : documents) {
                if (doc.contains(term)) {
                    docFrequency++;
                }
            }
            // Smoothed IDF so unseen/rare terms don't produce a divide-by-zero
            idf.put(term, Math.log((totalDocs + 1.0) / (docFrequency + 1.0)) + 1.0);
        }
        return idf;
    }

    private double[] vectorize(List<String> tokens, List<String> vocabulary, Map<String, Double> idf) {
        double[] vector = new double[vocabulary.size()];
        if (tokens.isEmpty()) {
            return vector;
        }

        Map<String, Integer> termCounts = new HashMap<>();
        for (String token : tokens) {
            termCounts.merge(token, 1, Integer::sum);
        }

        for (int i = 0; i < vocabulary.size(); i++) {
            String term = vocabulary.get(i);
            int count = termCounts.getOrDefault(term, 0);
            if (count > 0) {
                double tf = (double) count / tokens.size();
                vector[i] = tf * idf.getOrDefault(term, 0.0);
            }
        }
        return vector;
    }

    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
