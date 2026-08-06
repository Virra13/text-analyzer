package ru.virra.textanalyzer.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.virra.textanalyzer.analysis.application.AnalysisConfig;
import ru.virra.textanalyzer.analysis.application.ApplicationService;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncAnalysisService {
    private final ApplicationService applicationService;
    private final AnalysisPersistenceService analysisPersistenceService;

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
