package ru.virra.textanalyzer.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.virra.textanalyzer.analysis.application.AnalysisConfig;
import ru.virra.textanalyzer.analysis.application.ApplicationService;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;

import java.util.UUID;

/**
 * Выполняет анализ текстов в асинхронном режиме.
 *
 * <p>Сервис восстанавливает конфигурацию анализа, изменяет его статус,
 * запускает обработку файлов и сохраняет итоговый результат.
 * При неожиданной ошибке анализ переводится в состояние
 * {@link AnalysisStatus#FAILED}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncAnalysisService {
    private final ApplicationService applicationService;
    private final AnalysisPersistenceService analysisPersistenceService;

    /**
     * Асинхронно выполняет анализ с указанным идентификатором.
     *
     * <p>Перед началом обработки анализ переводится в статус
     * {@link AnalysisStatus#RUNNING}. После успешного завершения
     * результат сохраняется в базе данных. При ошибке анализ
     * переводится в статус {@link AnalysisStatus#FAILED}.</p>
     *
     * @param analysisId идентификатор анализа
     */
    @Async
    public void analyze(UUID analysisId) {
        try {
            AnalysisConfig config = analysisPersistenceService.getConfig(analysisId);
            analysisPersistenceService.markRunning(analysisId);
            AnalysisResult analysisResult = applicationService.go(config);
            analysisPersistenceService.complete(analysisId, analysisResult);

        } catch (RuntimeException e) {
            analysisPersistenceService.markFailed(analysisId);
            log.error("Analysis {} failed", analysisId, e);
        }
    }
}