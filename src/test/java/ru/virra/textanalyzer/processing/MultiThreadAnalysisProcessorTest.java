package ru.virra.textanalyzer.processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.virra.textanalyzer.analyzer.Analyzer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MultiThreadAnalysisProcessorTest {

    @Mock
    private Analyzer analyzer;

    @InjectMocks
    private MultiThreadAnalysisProcessor processor;

    @Test
    void processMergesResultsFromAllTexts() {
        String text1 = "java spring java";
        String text2 = "java thread thread";

        Set<String> stopWords = Set.of();

        when(analyzer.analyze(List.of(text1), stopWords, 3))
                .thenReturn(Map.of(
                        "java", 2,
                        "spring", 1
                ));

        when(analyzer.analyze(List.of(text2), stopWords, 3))
                .thenReturn(Map.of(
                        "java", 1,
                        "thread", 2
                ));

        Map<String, Integer> result = processor.process(
                List.of(text1, text2),
                stopWords,
                3,
                2
        );

        Map<String, Integer> expected = Map.of(
                "java", 3,
                "spring", 1,
                "thread", 2
        );

        assertEquals(expected, result);
    }

    @Test
    void processAnalyzesEachTextSeparately() {
        String text1 = "first text";
        String text2 = "second text";

        Set<String> stopWords = Set.of();

        when(analyzer.analyze(List.of(text1), stopWords, 3))
                .thenReturn(Map.of("first", 1));

        when(analyzer.analyze(List.of(text2), stopWords, 3))
                .thenReturn(Map.of("second", 1));

        processor.process(
                List.of(text1, text2),
                stopWords,
                3,
                2
        );

        verify(analyzer).analyze(List.of(text1), stopWords, 3);
        verify(analyzer).analyze(List.of(text2), stopWords, 3);
    }

    @Test
    void processSumsSameWordCountsFromDifferentTexts() {
        String text = "java";

        Set<String> stopWords = Set.of();

        when(analyzer.analyze(List.of(text), stopWords, 3))
                .thenReturn(Map.of("java", 5))
                .thenReturn(Map.of("java", 7));

        Map<String, Integer> result = processor.process(
                List.of(text, text),
                stopWords,
                3,
                2
        );

        assertEquals(12, result.get("java"));
    }

    @Test
    void processWithEmptyTextsReturnsEmptyMap() {
        Map<String, Integer> result = processor.process(
                List.of(),
                Set.of(),
                3,
                2
        );

        assertEquals(Map.of(), result);
    }
}