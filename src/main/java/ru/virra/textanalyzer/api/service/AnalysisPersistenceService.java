package ru.virra.textanalyzer.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.virra.textanalyzer.analysis.application.AnalysisConfig;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;
import ru.virra.textanalyzer.analysis.model.AnalysisInfo;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.analysis.model.FileReadError;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;
import ru.virra.textanalyzer.persistence.entity.FileErrorEntity;
import ru.virra.textanalyzer.persistence.entity.WordResultEntity;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Управляет сохранением состояния и результатов анализа в базе данных.
 *
 * <p>Сервис отвечает за изменение статуса анализа, восстановление
 * конфигурации для выполнения и сохранение итоговых результатов,
 * включая частоты слов и ошибки обработки файлов.</p>
 */
@Service
@RequiredArgsConstructor
public class AnalysisPersistenceService {

    private final AnalysisRepository analysisRepository;

    /**
     * Переводит анализ в состояние выполнения.
     *
     * @param analysisId идентификатор анализа
     */
    @Transactional
    public void markRunning(UUID analysisId) {
        AnalysisEntity analysis = analysisRepository.findById(analysisId).orElseThrow();
        analysis.setStatus(AnalysisStatus.RUNNING);
        analysis.setStartedAt(LocalDateTime.now());
        analysisRepository.save(analysis);
    }

    /**
     * Восстанавливает конфигурацию анализа по сохранённым параметрам.
     *
     * @param analysisId идентификатор анализа
     * @return конфигурация для выполнения анализа
     */
    @Transactional(readOnly = true)
    public AnalysisConfig getConfig(UUID analysisId) {
        AnalysisEntity analysis = analysisRepository.findById(analysisId).orElseThrow();

        return AnalysisConfig.builder()
                .directory(Path.of(analysis.getDirectory()))
                .minLength(analysis.getMinWordLength())
                .top(analysis.getTopCount())
                .output(null)
                .stopWords(analysis.getStopWords() != null ? Path.of(analysis.getStopWords()) : null)
                .threads(analysis.getThreads())
                .mode(analysis.getMode())
                .build();
    }

    /**
     * Сохраняет результат успешно завершённого анализа.
     *
     * <p>Сохраняет частоты слов, ошибки обработки файлов,
     * количество обработанных файлов, время выполнения
     * и переводит анализ в статус {@link AnalysisStatus#COMPLETED}.</p>
     *
     * @param analysisId идентификатор анализа
     * @param analysisResult результат выполненного анализа
     */
    @Transactional
    public void complete(UUID analysisId, AnalysisResult analysisResult) {
        AnalysisEntity analysis = analysisRepository.findById(analysisId).orElseThrow();

        for (var word : analysisResult.wordCount()) {
            WordResultEntity wordEntity = new WordResultEntity();
            wordEntity.setWord(word.word());
            wordEntity.setWordCount(word.count());
            analysis.addWord(wordEntity);
        }

        for (var error : analysisResult.errors()) {
            FileErrorEntity errorEntity = new FileErrorEntity();
            errorEntity.setFileName(error.fileName());
            errorEntity.setMessage(error.message());
            analysis.addError(errorEntity);
        }

        AnalysisInfo info = analysisResult.analysisInfo();

        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setCompletedAt(LocalDateTime.now());
        analysis.setExecutionTimeMs(info.executionTimeMs());
        analysis.setProcessedFiles(info.processedFiles());
        analysisRepository.save(analysis);
    }

    /**
     * Переводит анализ в состояние ошибки.
     *
     * @param analysisId идентификатор анализа
     */
    @Transactional
    public void markFailed(UUID analysisId, String failureMessage) {
        analysisRepository.findById(analysisId).ifPresent(analysis -> {
            analysis.setStatus(AnalysisStatus.FAILED);
            analysis.setFailureMessage(failureMessage);
            analysis.setCompletedAt(LocalDateTime.now());
            analysisRepository.save(analysis);
        });
    }
}
