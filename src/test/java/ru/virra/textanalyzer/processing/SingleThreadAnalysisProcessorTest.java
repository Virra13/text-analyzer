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
class SingleThreadAnalysisProcessorTest {

    @Mock
    private Analyzer analyzer;

    @InjectMocks
    private SingleThreadAnalysisProcessor processor;

    @Test
    void processReturnsAnalyzerResult() {
        List<String> texts = List.of(
                "java spring java",
                "spring boot"
        );

        Set<String> stopWords = Set.of("boot");

        Map<String, Integer> expected = Map.of(
                "java", 2,
                "spring", 2
        );

        when(analyzer.analyze(texts, stopWords, 3))
                .thenReturn(expected);

        Map<String, Integer> result =
                processor.process(texts, stopWords, 3, 2);

        assertEquals(expected, result);

        verify(analyzer).analyze(texts, stopWords, 3);
    }
}