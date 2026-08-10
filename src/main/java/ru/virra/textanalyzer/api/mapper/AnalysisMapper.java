package ru.virra.textanalyzer.api.mapper;

import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;
import ru.virra.textanalyzer.api.dto.AnalysisResponse;
import ru.virra.textanalyzer.analysis.model.AnalysisInfo;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.analysis.model.FileReadError;
import ru.virra.textanalyzer.analysis.model.WordCount;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;

import java.nio.file.Path;
import java.util.List;

@Component
public class AnalysisMapper {

    public AnalysisResponse toResponse(AnalysisEntity analysis) {
        if (analysis.getStatus() != AnalysisStatus.COMPLETED) {
            return new AnalysisResponse(
                    analysis.getId(),
                    analysis.getStatus(),
                    null
            );
        }

        List<WordCount> words = analysis.getWords().stream()
                .map(word -> new WordCount(
                        word.getWord(),
                        word.getWordCount()
                ))
                .toList();

        List<FileReadError> errors = analysis.getErrors().stream()
                .map(error -> new FileReadError(
                        error.getFileName(),
                        error.getMessage()
                ))
                .toList();

        AnalysisInfo info = new AnalysisInfo(
                Path.of(analysis.getDirectory()),
                analysis.getMinWordLength(),
                analysis.getTopCount(),
                ExecutionMode.fromString(analysis.getMode()),
                analysis.getThreads(),
                analysis.getProcessedFiles(),
                analysis.getExecutionTimeMs()
        );

        AnalysisResult result = new AnalysisResult(
                info,
                words,
                errors
        );

        return new AnalysisResponse(
                analysis.getId(),
                analysis.getStatus(),
                result
        );
    }
}