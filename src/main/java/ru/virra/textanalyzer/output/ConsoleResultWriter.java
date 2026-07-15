package ru.virra.textanalyzer.output;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.model.WordCount;

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
    public void write(List<WordCount> list) {

        int i = 1;
        for (var wordCount : list) {
            System.out.printf("%d. %s - %d%n", i, wordCount.word(), wordCount.count());
            i++;
        }
        log.info("Analysis result written to console: {} entries", list.size());
    }
}
