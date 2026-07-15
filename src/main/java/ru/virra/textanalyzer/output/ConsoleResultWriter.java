package ru.virra.textanalyzer.output;

import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.application.WordCount;

import java.util.List;

@Component
public class ConsoleResultWriter  {

    public void write(List<WordCount> list) {
        int i = 1;
        for (var wordCount : list) {
            System.out.printf("%d. %s - %d%n", i, wordCount.word(), wordCount.count());
            i++;
        }
    }
}
