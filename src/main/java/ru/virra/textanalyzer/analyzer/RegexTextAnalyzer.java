package ru.virra.textanalyzer.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реализация {@link Analyzer}, извлекающая слова из текста
 * с помощью регулярного выражения.
 *
 * <p>При анализе слова приводятся к нижнему регистру,
 * фильтруются по минимальной длине и исключаются,
 * если входят в набор стоп-слов.</p>
 */
@Slf4j
@Component
public class RegexTextAnalyzer implements Analyzer{

    private static final Pattern WORD_PATTERN = Pattern.compile("\\p{L}+(?:[-_'’]\\p{L}+)*");

    /**
     * Подсчитывает количество вхождений слов в переданных текстах.
     *
     * @param texts     тексты для анализа
     * @param stopWords слова, которые необходимо исключить из подсчёта
     * @param minLength минимальная длина учитываемого слова
     * @return отображение, где ключ — слово в нижнем регистре,
     *         а значение — количество его вхождений
     */
    public Map<String, Integer> analyze(Iterable<String> texts, Set<String> stopWords, int minLength) {
        log.info("Starting text analysis with minimum word length: {}", minLength);

        Map<String, Integer> result = new HashMap<>();
        int textCount = 0;
        int acceptedWordCount = 0;

        for (String text : texts) {
            textCount++;
            Matcher matcher = WORD_PATTERN.matcher(text);
            while (matcher.find()) {
                String word = matcher.group().toLowerCase(Locale.ROOT);

                if (word.length() >= minLength && !stopWords.contains(word)) {
                    result.merge(word, 1, Integer::sum);
                    acceptedWordCount++;
                }
            }
        }

        log.info(
                "Text analysis completed: {} texts processed, {} words accepted, {} unique words found",
                textCount,
                acceptedWordCount,
                result.size()
        );

        return result;
    }
}