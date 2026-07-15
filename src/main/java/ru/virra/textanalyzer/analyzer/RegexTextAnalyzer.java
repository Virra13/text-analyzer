package ru.virra.textanalyzer.analyzer;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RegexTextAnalyzer implements Analyzer{

    private static final Pattern WORD_PATTERN = Pattern.compile("\\p{L}+(?:[-_'’]\\p{L}+)*");

    public Map<String, Integer> analyze(Iterable<String> texts, Set<String> stopWords, int minLength) {

        Map<String, Integer> result = new HashMap<>();
        for (String text : texts) {

            Matcher matcher = WORD_PATTERN.matcher(text);
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