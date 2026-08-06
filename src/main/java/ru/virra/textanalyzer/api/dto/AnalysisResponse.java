package ru.virra.textanalyzer.api.dto;

import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;

import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        AnalysisStatus status,
        AnalysisResult result
) {
}
