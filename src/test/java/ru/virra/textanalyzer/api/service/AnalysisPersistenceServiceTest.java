package ru.virra.textanalyzer.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.virra.textanalyzer.analysis.application.AnalysisConfig;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;
import ru.virra.textanalyzer.analysis.model.AnalysisInfo;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.analysis.model.FileReadError;
import ru.virra.textanalyzer.analysis.model.WordCount;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisPersistenceServiceTest {

    @Mock
    private AnalysisRepository analysisRepository;
    private static final UUID ANALYSIS_ID = UUID.fromString("f5d01536-3394-446a-a845-83b97ff83045");

    private AnalysisPersistenceService analysisPersistenceService;

    @BeforeEach
    void setUp() {
        analysisPersistenceService = new AnalysisPersistenceService(analysisRepository);
    }

    @Test
    void shouldMarkAnalysisAsRunning() {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setId(ANALYSIS_ID);

        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.of(analysis));

        analysisPersistenceService.markRunning(ANALYSIS_ID);

        ArgumentCaptor<AnalysisEntity> captor = ArgumentCaptor.forClass(AnalysisEntity.class);
        verify(analysisRepository).save(captor.capture());

        AnalysisEntity savedAnalysis = captor.getValue();

        assertAll(
                () -> assertEquals(ANALYSIS_ID, savedAnalysis.getId()),
                () -> assertEquals(AnalysisStatus.RUNNING, savedAnalysis.getStatus()),
                () -> assertNotNull(savedAnalysis.getStartedAt())
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
    }

    @Test
    void shouldThrowExceptionWhenAnalysisNotFoundForMarkRunning() {
        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> analysisPersistenceService.markRunning(ANALYSIS_ID)
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
        verify(analysisRepository, never()).save(any(AnalysisEntity.class));
    }

    @Test
    void shouldGetConfigById() {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setId(ANALYSIS_ID);
        analysis.setDirectory("texts");
        analysis.setMinWordLength(3);
        analysis.setTopCount(10);
        analysis.setMode(ExecutionMode.MULTI);
        analysis.setThreads(4);
        analysis.setStopWords("config/stopwords.txt");

        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.of(analysis));

        AnalysisConfig config = analysisPersistenceService.getConfig(ANALYSIS_ID);

        assertAll(
                () -> assertEquals(Path.of("texts"), config.getDirectory()),
                () -> assertEquals(3, config.getMinLength()),
                () -> assertEquals(10, config.getTop()),
                () -> assertEquals(ExecutionMode.MULTI, config.getMode()),
                () -> assertEquals(4, config.getThreads()),
                () -> assertEquals(Path.of("config/stopwords.txt"), config.getStopWords()),
                () -> assertNull(config.getOutput())
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
    }

    @Test
    void shouldThrowExceptionWhenAnalysisNotFoundForGetConfig() {
        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> analysisPersistenceService.getConfig(ANALYSIS_ID)
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
    }

    @Test
    void shouldCompleteAnalysis() {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setId(ANALYSIS_ID);

        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.of(analysis));

        AnalysisInfo info = new AnalysisInfo(null, 1, 1,
                null, 1, 5, 200);
        List<WordCount> wordCount = List.of(
                new WordCount("кот", 2),
                new WordCount("собака", 3)
        );

        List<FileReadError> errors = List.of(
                new FileReadError("broken.txt", "Access denied"),
                new FileReadError("empty.txt", "Cannot read file")
        );

        AnalysisResult analysisResult = new AnalysisResult(info, wordCount, errors);

        analysisPersistenceService.complete(ANALYSIS_ID, analysisResult);

        ArgumentCaptor<AnalysisEntity> captor = ArgumentCaptor.forClass(AnalysisEntity.class);
        verify(analysisRepository).save(captor.capture());
        AnalysisEntity savedAnalysis = captor.getValue();

        assertAll(
                () -> assertEquals(AnalysisStatus.COMPLETED, savedAnalysis.getStatus()),
                () -> assertNotNull(savedAnalysis.getCompletedAt()),
                () -> assertEquals(200, savedAnalysis.getExecutionTimeMs()),
                () -> assertEquals(5, savedAnalysis.getProcessedFiles()),
                () -> assertEquals(2, savedAnalysis.getWords().size()),
                () -> assertEquals(ANALYSIS_ID, savedAnalysis.getId())
        );

        assertAll(
                () -> assertEquals("кот", savedAnalysis.getWords().get(0).getWord()),
                () -> assertEquals(2, savedAnalysis.getWords().get(0).getWordCount()),
                () -> assertEquals("собака", savedAnalysis.getWords().get(1).getWord()),
                () -> assertEquals(3, savedAnalysis.getWords().get(1).getWordCount())
        );

        assertAll(
                () -> assertEquals("broken.txt", savedAnalysis.getErrors().get(0).getFileName()),
                () -> assertEquals("Access denied", savedAnalysis.getErrors().get(0).getMessage()),
                () -> assertEquals("empty.txt", savedAnalysis.getErrors().get(1).getFileName()),
                () -> assertEquals("Cannot read file", savedAnalysis.getErrors().get(1).getMessage()),
                () -> assertSame(savedAnalysis, savedAnalysis.getErrors().get(0).getAnalysis()),
                () -> assertSame(savedAnalysis, savedAnalysis.getErrors().get(1).getAnalysis())
        );
    }

    @Test
    void shouldThrowExceptionWhenAnalysisNotFoundForComplete() {
        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.empty());

        AnalysisResult analysisResult = new AnalysisResult(
                new AnalysisInfo(null, 1, 1, null, 1, 5, 200),
                List.of(),
                List.of()
        );

        assertThrows(
                NoSuchElementException.class,
                () -> analysisPersistenceService.complete(ANALYSIS_ID, analysisResult)
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
        verify(analysisRepository, never()).save(any(AnalysisEntity.class));
    }

    @Test
    void shouldMarkAnalysisAsFailed() {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setId(ANALYSIS_ID);

        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.of(analysis));

        analysisPersistenceService.markFailed(ANALYSIS_ID);

        ArgumentCaptor<AnalysisEntity> captor = ArgumentCaptor.forClass(AnalysisEntity.class);
        verify(analysisRepository).save(captor.capture());

        AnalysisEntity savedAnalysis = captor.getValue();

        assertAll(
                () -> assertEquals(ANALYSIS_ID, savedAnalysis.getId()),
                () -> assertEquals(AnalysisStatus.FAILED, savedAnalysis.getStatus()),
                () -> assertNotNull(savedAnalysis.getCompletedAt())
        );

        verify(analysisRepository).findById(ANALYSIS_ID);
    }

    @Test
    void shouldDoNothingWhenAnalysisNotFoundForMarkFailed() {
        when(analysisRepository.findById(ANALYSIS_ID)).thenReturn(Optional.empty());

        analysisPersistenceService.markFailed(ANALYSIS_ID);

        verify(analysisRepository).findById(ANALYSIS_ID);
        verify(analysisRepository, never()).save(any(AnalysisEntity.class));
    }
}