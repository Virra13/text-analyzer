package ru.virra.textanalyzer.output;

import ru.virra.textanalyzer.application.WordCount;

import java.nio.file.Path;
import java.util.List;

public class JsonResultWriter {

        public void write(List<WordCount> list, Path path) {
            int i = 1;
            for (var wordCount : list) {
                System.out.printf("%d. %s — %d%n", i, wordCount.word(), wordCount.count());
                i++;
            }



        }


}