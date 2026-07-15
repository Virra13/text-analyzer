package ru.virra.textanalyzer.analyzer;

import java.util.Map;
import java.util.Set;

/**
 * Анализирует коллекцию текстов и подсчитывает частоту встречаемости слов.
 */
public interface Analyzer {

    /**
     * Выполняет анализ переданных текстов.
     *
     * @param texts     тексты для анализа
     * @param stopWords слова, которые необходимо исключить из подсчёта
     * @param minLength минимальная длина учитываемого слова
     * @return отображение, где ключ — слово, а значение — количество его вхождений
     */
    Map<String, Integer> analyze(Iterable<String> texts, Set<String> stopWords, int minLength);

}
