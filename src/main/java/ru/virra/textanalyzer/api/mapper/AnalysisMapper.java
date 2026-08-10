package ru.virra.textanalyzer.api.mapper;

import org.springframework.context.annotation.Profile;
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

/**
 * Преобразует сущности анализа из слоя хранения в модели REST API.
 */
@Component
@Profile("rest")
public class AnalysisMapper {

    /**
     * Преобразует сохранённый анализ в объект ответа API.
     *
     * <p>Если анализ ещё не завершён, возвращает только его идентификатор
     * и текущий статус. Для завершённого анализа также восстанавливает
     * информацию об анализе, частоты слов и ошибки обработки файлов.</p>
     *
     * @param analysis сущность анализа
     * @return представление анализа для REST API
     */
    public AnalysisResponse toResponse(AnalysisEntity analysis) {

        if (analysis.getStatus() != AnalysisStatus.COMPLETED) {
            return new AnalysisResponse(analysis.getId(), analysis.getStatus(),null, analysis.getFailureMessage());
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
                analysis.getMode(),
                analysis.getThreads(),
                analysis.getProcessedFiles(),
                analysis.getExecutionTimeMs()
        );

        AnalysisResult result = new AnalysisResult(info, words, errors);

        return new AnalysisResponse(analysis.getId(), analysis.getStatus(), result,  null);
    }
}