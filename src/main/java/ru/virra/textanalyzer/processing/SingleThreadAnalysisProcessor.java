package ru.virra.textanalyzer.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analyzer.Analyzer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Component("single")
@RequiredArgsConstructor
public class SingleThreadAnalysisProcessor implements AnalysisProcessor {
    private final Analyzer analyzer;

    @Override
    public Map<String, Integer> process(Collection<String> texts, Set<String> stopWords, int minLength, int threads) {
        return analyzer.analyze(texts, stopWords, minLength);
    }
}
