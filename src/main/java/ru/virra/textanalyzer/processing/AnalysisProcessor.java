package ru.virra.textanalyzer.processing;

import ru.virra.textanalyzer.application.AnalysisConfig;
import ru.virra.textanalyzer.model.AnalysisResult;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface AnalysisProcessor {
    Map<String, Integer> process(Collection<String> texts, Set<String> stopWords, int minLength, int threads);

}
