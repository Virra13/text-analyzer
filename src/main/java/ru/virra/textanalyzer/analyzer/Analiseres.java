package ru.virra.textanalyzer.analyzer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analiseres implements Analiserd {

    @Override
    public Map<String, Integer> analise(Iterable<String> texts, Set<String> stopWords, int minLength) {

        Map<String, Integer> result = new HashMap<>();
        Pattern pattern = Pattern.compile("\\p{L}+(?:[-_'’]\\p{L}+)*");

        for (String text : texts) {

            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String word = matcher.group().toLowerCase(Locale.ROOT);

                if (word.length() >= minLength && !stopWords.contains(word)) {
                    result.merge(word, 1, Integer::sum);
                }

            }
        }

        return result;
    }
}
