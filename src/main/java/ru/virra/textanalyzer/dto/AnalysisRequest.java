package ru.virra.textanalyzer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnalysisRequest(
        @NotBlank
        String directory,

        @Min(1)
        int minWordLength,

        @Min(1)
        int topCount,

        @NotNull
        String mode,

        @Min(1)
        int threads
) {
}
