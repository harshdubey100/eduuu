package com.eduverse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RagAnswerResponseDTO {
    private String question;
    private List<RetrievedChunkDTO> matches;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievedChunkDTO {
        private String lessonTitle;
        private String excerpt;
        private double relevanceScore;
    }
}
