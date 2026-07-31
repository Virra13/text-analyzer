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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Читает и анализирует текстовые файлы параллельно с использованием пула потоков.
 *
 * <p>Для каждого файла создаётся отдельная задача. Каждая задача формирует
 * независимый результат анализа, после чего результаты объединяются
 * в управляющем потоке.</p>
 */
@Slf4j
@Component("multi")
@RequiredArgsConstructor
public class MultiThreadAnalysisProcessor implements AnalysisProcessor {

    private final TextReader textReader;
    private final Analyzer analyzer;

    @Override
    public ProcessingResult process(Collection<Path> files, Set<String> stopWords, int minLength, int threads) {
        log.info("Starting multi-threaded processing of {} files using {} workers", files.size(), threads);
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<FileProcessingResult>> futures = new ArrayList<>();

            for (Path file : files) {
                log.debug("Submitting file for processing: {}", file);
                Future<FileProcessingResult> future =
                        executor.submit(() ->
                                processFile(
                                        file,
                                        stopWords,
                                        minLength
                                )
                        );

                futures.add(future);
            }
            Map<String, Integer> result = new HashMap<>();
            Map<Path, String> readErrors = new HashMap<>();
            int processedFiles = 0;

            for (Future<FileProcessingResult> future : futures) {
                FileProcessingResult fileResult = future.get();

                if (fileResult.error() != null) {
                    readErrors.put(
                            fileResult.file(),
                            fileResult.error()
                    );
                } else {
                    merge(result, fileResult.wordCounts());
                    processedFiles++;
                }
            }
            log.info("Multi-threaded processing completed. Processed: {}, errors: {}", processedFiles, readErrors.size());
            return new ProcessingResult(result, readErrors, processedFiles);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Text analysis was interrupted", e);

        } catch (ExecutionException e) {
            throw new RuntimeException("Error during text analysis", e);

        }
    }

    private FileProcessingResult processFile(Path file, Set<String> stopWords, int minLength) {

        log.debug("Processing file in worker thread: {}", file);
        try {
            String text = textReader.read(file);

            Map<String, Integer> result =
                    analyzer.analyze(
                            List.of(text),
                            stopWords,
                            minLength
                    );
            log.debug("File processed successfully: {}", file);
            return new FileProcessingResult(file, result, null);

        } catch (FileProcessingException e) {
            log.warn("Failed to process file {}: {}", file, e.getMessage());
            return new FileProcessingResult(file, Map.of(), e.getMessage());
        }
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

    private record FileProcessingResult(Path file, Map<String, Integer> wordCounts, String error) {
    }

}