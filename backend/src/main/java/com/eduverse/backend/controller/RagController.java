package com.eduverse.backend.controller;

import com.eduverse.backend.dto.RagAnswerResponseDTO;
import com.eduverse.backend.dto.RagQueryRequestDTO;
import com.eduverse.backend.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    // Semantic search over a course's lesson content - the retrieval half of RAG.
    // Returns the most relevant chunks rather than a generated answer (see RagService javadoc).
    @PostMapping("/courses/{courseId}/ask")
    public ResponseEntity<RagAnswerResponseDTO> ask(@PathVariable Long courseId, @Valid @RequestBody RagQueryRequestDTO dto) {
        return ResponseEntity.ok(ragService.search(courseId, dto.getQuestion()));
    }
}
