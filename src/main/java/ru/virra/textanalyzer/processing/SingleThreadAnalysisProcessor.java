package ru.virra.textanalyzer.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analyzer.Analyzer;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.input.TextReader;
import ru.virra.textanalyzer.model.ProcessingResult;

import java.nio.file.Path;
import java.util.*;

/**
 * Последовательно читает и анализирует текстовые файлы в одном потоке.
 */
@Slf4j
@Component("single")
@RequiredArgsConstructor
public class SingleThreadAnalysisProcessor implements AnalysisProcessor {
    private final TextReader textReader;
    private final Analyzer analyzer;

    @Override
    public ProcessingResult process(Collection<Path> files, Set<String> stopWords, int minLength, int threads) {
        log.info("Starting single-threaded processing of {} files", files.size());

        Map<String, Integer> result = new HashMap<>();
        Map<Path, String> readErrors = new HashMap<>();

        int processedFiles = 0;

        for (Path file : files) {
            log.debug("Processing file: {}", file);
            try {
                String text = textReader.read(file);

                Map<String, Integer> localResult =
                        analyzer.analyze(
                                List.of(text),
                                stopWords,
                                minLength
                        );

                merge(result, localResult);
                processedFiles++;

                log.debug("File processed successfully: {}", file);

            } catch (FileProcessingException e) {
                readErrors.put(file, e.getMessage());
            }
        }
        log.info("Single-threaded processing completed. Processed: {}, errors: {}", processedFiles, readErrors.size());
        return new ProcessingResult(result, readErrors, processedFiles);
    }

    private void merge(Map<String, Integer> result, Map<String, Integer> localResult) {
        for (Map.Entry<String, Integer> entry : localResult.entrySet()) {
            result.merge(
                    entry.getKey(),
                    entry.getValue(),
                    Integer::sum
            );
        }
    }
}

