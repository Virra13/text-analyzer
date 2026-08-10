package ru.virra.textanalyzer.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;

public record AnalysisRequest(
        @NotBlank
        String directory,

        @Min(1)
        int minWordLength,

        @Min(1)
        int topCount,

        @NotNull
        ExecutionMode mode,

        @Min(1)
        Integer threads,

        String stopWords
) {

        public int resolvedThreads() {
                return threads != null ? threads : 2;
        }

}
