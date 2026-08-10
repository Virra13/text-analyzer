package ru.virra.textanalyzer.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;
import ru.virra.textanalyzer.api.dto.AnalysisResponse;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;
import ru.virra.textanalyzer.persistence.entity.FileErrorEntity;
import ru.virra.textanalyzer.persistence.entity.WordResultEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisMapperTest {

    private static final UUID ANALYSIS_ID = UUID.fromString("f5d01536-3394-446a-a845-83b97ff83045");
    private AnalysisMapper analysisMapper;


    @BeforeEach
    void setUp() {
        analysisMapper = new AnalysisMapper();
    }

    @ParameterizedTest
    @EnumSource(
            value = AnalysisStatus.class,
            names = {"PENDING", "RUNNING", "FAILED"}
    )
    void shouldReturnNullResultForUncompletedAnalysis(AnalysisStatus status) {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setId(ANALYSIS_ID);
        analysis.setStatus(status);

        AnalysisResponse analysisResponse = analysisMapper.toResponse(analysis);

        assertAll(
                () -> assertEquals(ANALYSIS_ID, analysisResponse.id()),
                () -> assertEquals(status, analysisResponse.status()),
                () -> assertNull(analysisResponse.result())
        );
    }

    @Test
    void shouldReturnResultForCompletedAnalysis() {
        AnalysisEntity analysis = new AnalysisEntity();
        analysis.setId(ANALYSIS_ID);
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setDirectory("texts");
        analysis.setMinWordLength(3);
        analysis.setTopCount(10);
        analysis.setMode(ExecutionMode.MULTI);
        analysis.setThreads(4);
        analysis.setProcessedFiles(3);
        analysis.setExecutionTimeMs(7L);

        WordResultEntity word = new WordResultEntity();
        word.setWord("java");
        word.setWordCount(5);
        analysis.addWord(word);

        FileErrorEntity error = new FileErrorEntity();
        error.setFileName("broken.txt");
        error.setMessage("Access denied");
        analysis.addError(error);

        AnalysisResponse analysisResponse = analysisMapper.toResponse(analysis);

        assertNotNull(analysisResponse.result());

        assertAll(
                () -> assertEquals(ANALYSIS_ID, analysisResponse.id()),
                () -> assertEquals(AnalysisStatus.COMPLETED, analysisResponse.status())
        );

        assertAll(
                () -> assertEquals(1, analysisResponse.result().wordCount().size()),
                () -> assertEquals("java", analysisResponse.result().wordCount().get(0).word()),
                () -> assertEquals(5, analysisResponse.result().wordCount().get(0).count())
        );

        assertAll(
                () -> assertEquals(1, analysisResponse.result().errors().size()),
                () -> assertEquals("broken.txt", analysisResponse.result().errors().get(0).fileName()),
                () -> assertEquals("Access denied", analysisResponse.result().errors().get(0).message())
        );

        assertAll(
                () -> assertEquals("texts", analysisResponse.result().analysisInfo().directory().toString()),
                () -> assertEquals(3, analysisResponse.result().analysisInfo().minWordLength()),
                () -> assertEquals(10, analysisResponse.result().analysisInfo().topCount()),
                () -> assertEquals(ExecutionMode.MULTI, analysisResponse.result().analysisInfo().mode()),
                () -> assertEquals(4, analysisResponse.result().analysisInfo().threads()),
                () -> assertEquals(3, analysisResponse.result().analysisInfo().processedFiles()),
                () -> assertEquals(7L, analysisResponse.result().analysisInfo().executionTimeMs())
        );
    }
}