package ru.virra.textanalyzer.analyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.virra.textanalyzer.analysis.analyzer.RegexTextAnalyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegexTextAnalyzerTest {

    private RegexTextAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new RegexTextAnalyzer();
    }

    @Test
    void shouldCountWords() {
        List<String> texts = List.of("кот собака кот.  Кот? собака, мышь! когда-нибудь...");

        Map<String, Integer> result =
                analyzer.analyze(texts, Set.of(), 1);

        assertEquals(3, result.get("кот"));
        assertEquals(2, result.get("собака"));
        assertEquals(1, result.get("мышь"));
        assertEquals(1, result.get("когда-нибудь"));
        assertEquals(4, result.size());
    }

    @Test
    void shouldIgnoreCase() {
        List<String> texts = List.of(
                "Кот КОТ кот СобАка СОБАКА"
        );

        Map<String, Integer> result =
                analyzer.analyze(texts, Set.of(), 1);

        assertEquals(3, result.get("кот"));
        assertEquals(2, result.get("собака"));
        assertEquals(2, result.size());
    }

    @Test
    void shouldIgnoreWordsShorterThanMinLength() {
        List<String> texts = List.of(
                "я он кот собака"
        );

        Map<String, Integer> result =
                analyzer.analyze(texts, Set.of(), 3);

        assertEquals(1, result.get("кот"));
        assertEquals(1, result.get("собака"));
        assertEquals(2, result.size());
    }

    @Test
    void shouldExcludeStopWords() {
        List<String> texts = List.of(
                "кот собака кот птица"
        );

        Set<String> stopWords =
                Set.of("кот", "птица");

        Map<String, Integer> result =
                analyzer.analyze(texts, stopWords, 1);

        assertEquals(1, result.get("собака"));
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyMapForEmptyTexts() {
        Map<String, Integer> result =
                analyzer.analyze(List.of(), Set.of(), 1);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyMapWhenAllWordsAreFiltered() {
        List<String> texts = List.of(
                "кот собака птица"
        );

        Map<String, Integer> result =
                analyzer.analyze(
                        texts,
                        Set.of("кот", "собака", "птица"),
                        1
                );

        assertTrue(result.isEmpty());
    }
}