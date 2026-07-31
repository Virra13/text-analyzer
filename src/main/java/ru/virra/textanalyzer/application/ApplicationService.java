package ru.virra.textanalyzer.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.input.DirectoryScanner;
import ru.virra.textanalyzer.model.*;
import ru.virra.textanalyzer.input.StopWordsReader;
import ru.virra.textanalyzer.input.TextReader;
import ru.virra.textanalyzer.output.ConsoleResultWriter;
import ru.virra.textanalyzer.output.JsonResultWriter;
import ru.virra.textanalyzer.processing.AnalysisProcessor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Координирует основной сценарий анализа текстов.
 *
 * <p>Сервис читает текстовые файлы и стоп-слова, запускает анализ,
 * формирует итоговый результат и передаёт его выбранному компоненту вывода.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationService {

    private final DirectoryScanner directoryScanner;
    private final Map<String, AnalysisProcessor> processors;
    private final StopWordsReader stopWordsReader;
    private final ConsoleResultWriter consoleResultWriter;
    private final JsonResultWriter jsonResultWriter;

    /**
     * Выполняет обработку текстовых файлов согласно конфигурации:
     * сканирует директорию, загружает стоп-слова, выбирает стратегию обработки,
     * формирует итоговый результат и передаёт его выбранному writer'у.
     *
     * @param config конфигурация анализа
     */
    public void go(AnalysisConfig config) {

        log.info("Scanning text files from directory: {}", config.getDirectory());
        List<Path> files = directoryScanner.scan(config.getDirectory());

        log.info("Loading stop words from: {}", config.getStopWords());
        Set<String> stopWords = stopWordsReader.loadStopWords(config.getStopWords());

        AnalysisProcessor processor = processors.get(config.getMode().getValue());

        if (processor == null) {
            throw new IllegalStateException("No processor found for mode: " + config.getMode());
        }

        log.info("Starting text analysis in {} mode", config.getMode());

        long start = System.nanoTime();

        ProcessingResult processingResult = processor.process(files, stopWords, config.getMinLength(), config.getThreads());

        long executionTimeMs = (System.nanoTime() - start) / 1_000_000;

        Map<String, Integer> result = processingResult.wordCounts();

        log.info("Analysis completed. Found {} unique words.", result.size());

        List<WordCount> resultList = result.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(config.getTop())
                .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
                .toList();

        log.debug("Prepared {} result entries", resultList.size());

        List<FileReadError> errors = processingResult.readErrors().entrySet().stream()
                .map(entry -> new FileReadError(entry.getKey().getFileName().toString(), entry.getValue()))
                .toList();

        if (!errors.isEmpty()) {
            log.warn("Completed with {} file read errors", errors.size());
        }

        AnalysisInfo info = new AnalysisInfo(
                config.getDirectory(),
                config.getMinLength(),
                config.getTop(),
                config.getMode(),
                config.getThreads(),
                processingResult.processedFiles(),
                executionTimeMs
        );

        AnalysisResult analysisResult = new AnalysisResult(info, resultList, errors);

        if (config.getOutput() != null) {
            jsonResultWriter.write(analysisResult, config.getOutput());
            log.info("Writing result to JSON file: {}", config.getOutput());
        } else {
            consoleResultWriter.write(analysisResult);
            log.info("Writing result to console");
        }

        log.info("Text analysis finished successfully");
    }
}