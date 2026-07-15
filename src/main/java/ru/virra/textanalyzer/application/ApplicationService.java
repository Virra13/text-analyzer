package ru.virra.textanalyzer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analyzer.Analyzer;
import ru.virra.textanalyzer.input.ReadResult;
import ru.virra.textanalyzer.input.StopWordsReader;
import ru.virra.textanalyzer.input.TextReader;
import ru.virra.textanalyzer.input.TxtFileReader;
import ru.virra.textanalyzer.model.WordCount;
import ru.virra.textanalyzer.output.ConsoleResultWriter;
import ru.virra.textanalyzer.output.JsonResultWriter;

import java.util.List;
import java.util.Map;

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
        Map<String, Integer> result = analyzer.analize(readResult.texts().values(), stopWordsReader.loadStopWords(config.getStopWords()), config.getMinLength());

        List<WordCount> resultlist = result.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue()
                        .reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(config.getTop())
                .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
                .toList();

        if (config.getOutput() != null) {
            jsonResultWriter.write(resultlist, config.getOutput());
        } else {
            consoleResultWriter.write(resultlist);
        }
    }
}