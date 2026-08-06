package ru.virra.textanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.virra.textanalyzer.application.AnalysisConfig;
import ru.virra.textanalyzer.application.ApplicationService;
import ru.virra.textanalyzer.application.ExecutionMode;
import ru.virra.textanalyzer.model.AnalysisInfo;
import ru.virra.textanalyzer.model.AnalysisResult;
import ru.virra.textanalyzer.persistence.entity.AnalysisEntity;
import ru.virra.textanalyzer.persistence.entity.AnalysisStatus;
import ru.virra.textanalyzer.persistence.entity.WordsResultEntity;
import ru.virra.textanalyzer.persistence.repository.AnalysisRepository;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncAnalysisService {
    private final AnalysisRepository analysisRepository;
    private final ApplicationService applicationService;

    @Async
    @Transactional
    public void analyze(UUID analysisId) {

        AnalysisEntity analysis = analysisRepository.findById(analysisId).orElseThrow();
        try {
            analysis.setStatus(AnalysisStatus.RUNNING);
            analysis.setStartedAt(LocalDateTime.now());
            analysisRepository.save(analysis);

            AnalysisConfig config = AnalysisConfig.builder()
                    .directory(Path.of(analysis.getDirectory()))
                    .minLength(analysis.getMinWordLength())
                    .top(analysis.getTopCount())
                    .output(null)
                    .stopWords(null)
                    .threads(analysis.getThreads())
                    .mode(ExecutionMode.fromString(analysis.getMode()))
                    .build();

            AnalysisResult analysisResult = applicationService.go(config);

            for (var word : analysisResult.wordCount()) {
                WordsResultEntity wordEntity = new WordsResultEntity();
                wordEntity.setWord(word.word());
                wordEntity.setWordCount(word.count());
                analysis.addWord(wordEntity);
            }

            AnalysisInfo info = analysisResult.analysisInfo();

            analysis.setStatus(AnalysisStatus.COMPLETED);
            analysis.setCompletedAt(LocalDateTime.now());
            analysis.setExecutionTimeMs(info.executionTimeMs());
            analysis.setProcessedFiles(info.processedFiles());
            analysisRepository.save(analysis);

        } catch (RuntimeException e) {
            analysis.setStatus(AnalysisStatus.FAILED);
            analysis.setCompletedAt(LocalDateTime.now());
            analysisRepository.save(analysis);
            log.error("Analysis {} failed", analysisId, e);
        }
    }
}
