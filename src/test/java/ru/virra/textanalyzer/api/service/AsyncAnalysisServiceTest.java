package ru.virra.textanalyzer.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.virra.textanalyzer.analysis.application.AnalysisConfig;
import ru.virra.textanalyzer.analysis.application.ApplicationService;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncAnalysisServiceTest {

    private AsyncAnalysisService asyncAnalysisService;

    @Mock
    private ApplicationService applicationService;
    @Mock
    private AnalysisPersistenceService analysisPersistenceService;

    private static final UUID ANALYSIS_ID = UUID.fromString("f5d01536-3394-446a-a845-83b97ff83045");

    @BeforeEach
    void setUp() {
        asyncAnalysisService = new AsyncAnalysisService(applicationService, analysisPersistenceService);
    }

    @Test
    void shouldAnalyze() {

        AnalysisConfig config = mock(AnalysisConfig.class);
        when(analysisPersistenceService.getConfig(ANALYSIS_ID)).thenReturn(config);

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(applicationService.go(config)).thenReturn(analysisResult);

        asyncAnalysisService.analyze(ANALYSIS_ID);

        InOrder inOrder = inOrder(analysisPersistenceService,applicationService);

        inOrder.verify(analysisPersistenceService).getConfig(ANALYSIS_ID);
        inOrder.verify(analysisPersistenceService).markRunning(ANALYSIS_ID);
        inOrder.verify(applicationService).go(config);
        inOrder.verify(analysisPersistenceService).complete(ANALYSIS_ID, analysisResult);
        verify(analysisPersistenceService, never()).markFailed(ANALYSIS_ID);
    }

    @Test
    void shouldMarkFailedWhenThrowException() {

        AnalysisConfig config = mock(AnalysisConfig.class);
        when(analysisPersistenceService.getConfig(ANALYSIS_ID)).thenReturn(config);
        when(applicationService.go(config)).thenThrow(new RuntimeException("Analysis failed"));

        asyncAnalysisService.analyze(ANALYSIS_ID);

        InOrder inOrder = inOrder(analysisPersistenceService, applicationService);

        inOrder.verify(analysisPersistenceService).getConfig(ANALYSIS_ID);
        inOrder.verify(analysisPersistenceService).markRunning(ANALYSIS_ID);
        inOrder.verify(applicationService).go(config);
        inOrder.verify(analysisPersistenceService).markFailed(ANALYSIS_ID);

        verify(analysisPersistenceService, never()).complete(eq(ANALYSIS_ID), any(AnalysisResult.class));
    }
}