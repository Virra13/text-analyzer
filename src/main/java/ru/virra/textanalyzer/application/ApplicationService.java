package ru.virra.textanalyzer.application;

import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class ApplicationService {

    private final TextReader txtReader;
    private final Analyzer analyzer;
    private final StopWordsReader stopWordsReader;
    private final ConsoleResultWriter consoleResultWriter;
    private final JsonResultWriter jsonResultWriter;

    public void go(AnalysisConfig config) {

        ReadResult readResult = txtReader.read(config.getDirectory());
        Set<String> stopWords = stopWordsReader.loadStopWords(config.getStopWords());
        Map<String, Integer> result = analyzer.analyze(readResult.texts().values(), stopWords, config.getMinLength());

        List<WordCount> resultlist = result.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(config.getTop())
                .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
                .toList();

        List<FileReadError> errors = readResult.readErrors().entrySet().stream()
                .map(entry -> new FileReadError(entry.getKey().getFileName().toString(), entry.getValue()))
                .toList();

        AnalysisInfo info = new AnalysisInfo(config.getDirectory(), config.getMinLength(), config.getTop());
        AnalysisResult analysisResult = new AnalysisResult(info, resultlist, errors);

        if (config.getOutput() != null) {
            jsonResultWriter.write(analysisResult, config.getOutput());
        } else {
            consoleResultWriter.write(resultlist);
        }
    }
}