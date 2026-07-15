package ru.virra.textanalyzer.analyzer;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public interface Analiserd {

    Map<String, Integer> analise(Iterable<String> texts, Set<String> stopWords, int minLength);




}
