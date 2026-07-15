package ru.virra.textanalyzer.analyzer;

import java.util.Map;
import java.util.Set;

public interface Analyzer {

    public Map<String, Integer> analize(Iterable<String> texts, Set<String> stopWords, int minLength);

}
