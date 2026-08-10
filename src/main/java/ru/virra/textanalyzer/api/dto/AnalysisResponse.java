package ru.virra.textanalyzer.api.dto;

import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;

import java.util.UUID;

/**
 * Ответ REST API с информацией о состоянии анализа.
 *
 * <p>Для незавершённого анализа поле {@code result} имеет значение {@code null}.</p>
 *
 * @param id уникальный идентификатор анализа
 * @param status текущий статус анализа
 * @param result результат анализа или {@code null}, если анализ ещё не завершён
 */
public record AnalysisResponse(
        UUID id,
        AnalysisStatus status,
        AnalysisResult result
) {
}
