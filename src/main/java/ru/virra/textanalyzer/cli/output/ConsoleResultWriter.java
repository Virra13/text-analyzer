package ru.virra.textanalyzer.cli.output;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;
import ru.virra.textanalyzer.analysis.model.AnalysisInfo;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.analysis.model.WordCount;

import java.util.List;

/**
 * Выводит результаты анализа текста в консоль.
 */
@Slf4j
@Component
public class ConsoleResultWriter  {


    /**
     * Печатает список слов и количество их вхождений
     * в порядке, в котором они переданы в метод.
     *
     * @param list список результатов анализа
     */
    public void write(AnalysisResult analysisResult) {
        AnalysisInfo info = analysisResult.analysisInfo();

        if (info.mode() == ExecutionMode.MULTI) {
            System.out.printf("Mode: MULTI (%d workers)%n", info.threads());
        } else {
            System.out.println("Mode: SINGLE");
        }

        System.out.printf("Processed %d files in %d ms%n", info.processedFiles(), info.executionTimeMs());
        System.out.printf("Top %d words (min length = %d):%n", info.topCount(), info.minWordLength());

        List<WordCount> list = analysisResult.wordCount();

        int i = 1;
        for (var wordCount : list) {
            System.out.printf("%d. %s - %d%n", i, wordCount.word(), wordCount.count());
            i++;
        }
    }
}
