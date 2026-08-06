package ru.virra.textanalyzer.dto;

import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;

import java.util.UUID;

public record AnalysisStartResponse(
        UUID id,
        AnalysisStatus status
) {
}
