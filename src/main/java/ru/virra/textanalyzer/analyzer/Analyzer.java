package ru.virra.textanalyzer.analyzer;

import java.util.Map;
import java.util.Set;

public interface Analyzer {

    Map<String, Integer> analyze(Iterable<String> texts, Set<String> stopWords, int minLength);

}
