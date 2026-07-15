package ru.virra.textanalyzer.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analyzer.Analyzer;
import ru.virra.textanalyzer.model.ReadResult;
import ru.virra.textanalyzer.input.StopWordsReader;
import ru.virra.textanalyzer.input.TextReader;
import ru.virra.textanalyzer.model.AnalysisInfo;
import ru.virra.textanalyzer.model.AnalysisResult;
import ru.virra.textanalyzer.model.FileReadError;
import ru.virra.textanalyzer.model.WordCount;
import ru.virra.textanalyzer.output.ConsoleResultWriter;
import ru.virra.textanalyzer.output.JsonResultWriter;

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

    private final TextReader txtReader;
    private final Analyzer analyzer;
    private final StopWordsReader stopWordsReader;
    private final ConsoleResultWriter consoleResultWriter;
    private final JsonResultWriter jsonResultWriter;

    /**
     * Выполняет анализ текстов согласно переданной конфигурации.
     *
     * <p>Если в конфигурации указан выходной файл, результат сохраняется
     * в формате JSON. В противном случае результат выводится в консоль.</p>
     *
     * @param config конфигурация анализа
     */
    public void go(AnalysisConfig config) {

        log.info("Reading text files from directory: {}", config.getDirectory());
        ReadResult readResult = txtReader.read(config.getDirectory());

        log.info("Loading stop words from: {}", config.getStopWords());
        Set<String> stopWords = stopWordsReader.loadStopWords(config.getStopWords());

        log.info("Starting text analysis");
        Map<String, Integer> result = analyzer.analyze(readResult.texts().values(), stopWords, config.getMinLength());
        log.info("Analysis completed. Found {} unique words.", result.size());

        List<WordCount> resultlist = result.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(config.getTop())
                .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
                .toList();
        log.debug("Prepared {} result entries", resultlist.size());

        List<FileReadError> errors = readResult.readErrors().entrySet().stream()
                .map(entry -> new FileReadError(entry.getKey().getFileName().toString(), entry.getValue()))
                .toList();
        log.warn("Completed with {} file read errors", errors.size());

        AnalysisInfo info = new AnalysisInfo(config.getDirectory(), config.getMinLength(), config.getTop());
        AnalysisResult analysisResult = new AnalysisResult(info, resultlist, errors);

        if (config.getOutput() != null) {
            jsonResultWriter.write(analysisResult, config.getOutput());
            log.info("Writing result to JSON file: {}", config.getOutput());
        } else {
            consoleResultWriter.write(resultlist);
            log.info("Writing result to console");
        }

        log.info("Text analysis finished successfully");
    }
}