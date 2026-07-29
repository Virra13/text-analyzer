package ru.virra.textanalyzer.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.virra.textanalyzer.analyzer.Analyzer;
import ru.virra.textanalyzer.input.StopWordsReader;
import ru.virra.textanalyzer.input.TextReader;
import ru.virra.textanalyzer.model.AnalysisResult;
import ru.virra.textanalyzer.model.ReadResult;
import ru.virra.textanalyzer.model.WordCount;
import ru.virra.textanalyzer.output.ConsoleResultWriter;
import ru.virra.textanalyzer.output.JsonResultWriter;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {
    @Mock
    private TextReader txtReader;

    @Mock
    private Analyzer analyzer;

    @Mock
    private StopWordsReader stopWordsReader;

    @Mock
    private ConsoleResultWriter consoleResultWriter;

    @Mock
    private JsonResultWriter jsonResultWriter;

    @Captor
    private ArgumentCaptor<List<WordCount>> wordCountCaptor;

    @Captor
    private ArgumentCaptor<AnalysisResult> analysisResultCaptor;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(
                txtReader,
                analyzer,
                stopWordsReader,
                consoleResultWriter,
                jsonResultWriter
        );
    }

    @Test
    void shouldPassDataToAnalyzer() {
        Path directory = Path.of("texts");
        Path stopWordsPath = Path.of("stopwords.txt");

        AnalysisConfig config = mock(AnalysisConfig.class);

        when(config.getDirectory()).thenReturn(directory);
        when(config.getStopWords()).thenReturn(stopWordsPath);
        when(config.getMinLength()).thenReturn(4);
        when(config.getTop()).thenReturn(10);
        when(config.getOutput()).thenReturn(null);

        Map<Path, String> texts = Map.of(
                Path.of("first.txt"), "Первый текст",
                Path.of("second.txt"), "Второй текст"
        );

        ReadResult readResult = new ReadResult(
                texts,
                Map.of()
        );

        Set<String> stopWords = Set.of("первый", "второй");

        when(txtReader.read(directory)).thenReturn(readResult);
        when(stopWordsReader.loadStopWords(stopWordsPath)).thenReturn(stopWords);
        when(analyzer.analyze(texts.values(), stopWords, 4))
                .thenReturn(Map.of());

        applicationService.go(config);

        verify(txtReader).read(directory);
        verify(stopWordsReader).loadStopWords(stopWordsPath);
        verify(analyzer).analyze(texts.values(), stopWords, 4);
    }

    @Test
    void shouldSortWordsByCountDescending() {
        AnalysisConfig config = createConsoleConfig(10);

        when(txtReader.read(any())).thenReturn(
                new ReadResult(
                        Map.of(Path.of("text.txt"), "Текст"),
                        Map.of()
                )
        );

        when(stopWordsReader.loadStopWords(any()))
                .thenReturn(Set.of());

        when(analyzer.analyze(any(), any(), anyInt()))
                .thenReturn(Map.of(
                        "река", 2,
                        "лес", 5,
                        "ветер", 3
                ));

        applicationService.go(config);

        verify(consoleResultWriter).write(wordCountCaptor.capture());

        List<WordCount> result = wordCountCaptor.getValue();

        assertEquals("лес", result.get(0).word());
        assertEquals(5, result.get(0).count());

        assertEquals("ветер", result.get(1).word());
        assertEquals(3, result.get(1).count());

        assertEquals("река", result.get(2).word());
        assertEquals(2, result.get(2).count());
    }

    @Test
    void shouldSortAlphabeticallyWhenCountsAreEqual() {
        AnalysisConfig config = createConsoleConfig(10);

        when(txtReader.read(any())).thenReturn(
                new ReadResult(
                        Map.of(Path.of("text.txt"), "Текст"),
                        Map.of()
                )
        );

        when(stopWordsReader.loadStopWords(any()))
                .thenReturn(Set.of());

        when(analyzer.analyze(any(), any(), anyInt()))
                .thenReturn(Map.of(
                        "ветер", 3,
                        "берег", 3,
                        "река", 3
                ));

        applicationService.go(config);

        verify(consoleResultWriter).write(wordCountCaptor.capture());

        List<WordCount> result = wordCountCaptor.getValue();

        assertEquals("берег", result.get(0).word());
        assertEquals("ветер", result.get(1).word());
        assertEquals("река", result.get(2).word());
    }

    @Test
    void shouldLimitResultByTop() {
        AnalysisConfig config = createConsoleConfig(2);

        when(txtReader.read(any())).thenReturn(
                new ReadResult(
                        Map.of(Path.of("text.txt"), "Текст"),
                        Map.of()
                )
        );

        when(stopWordsReader.loadStopWords(any()))
                .thenReturn(Set.of());

        when(analyzer.analyze(any(), any(), anyInt()))
                .thenReturn(Map.of(
                        "город", 7,
                        "улица", 5,
                        "машина", 4,
                        "окно", 2
                ));

        applicationService.go(config);

        verify(consoleResultWriter).write(wordCountCaptor.capture());

        List<WordCount> result = wordCountCaptor.getValue();

        assertEquals(2, result.size());
        assertEquals("город", result.get(0).word());
        assertEquals("улица", result.get(1).word());
    }

    @Test
    void shouldUseConsoleWriterWhenOutputIsNull() {
        AnalysisConfig config = createConsoleConfig(10);

        when(txtReader.read(any())).thenReturn(
                new ReadResult(
                        Map.of(Path.of("text.txt"), "Текст"),
                        Map.of()
                )
        );

        when(stopWordsReader.loadStopWords(any()))
                .thenReturn(Set.of());

        when(analyzer.analyze(any(), any(), anyInt()))
                .thenReturn(Map.of("слово", 1));

        applicationService.go(config);

        verify(consoleResultWriter).write(any());
        verify(jsonResultWriter, never()).write(any(), any());
    }

    @Test
    void shouldUseJsonWriterWhenOutputIsSpecified() {
        Path output = Path.of("result.json");

        AnalysisConfig config = createJsonConfig(output, 10);

        when(txtReader.read(any())).thenReturn(
                new ReadResult(
                        Map.of(Path.of("text.txt"), "Текст"),
                        Map.of()
                )
        );

        when(stopWordsReader.loadStopWords(any()))
                .thenReturn(Set.of());

        when(analyzer.analyze(any(), any(), anyInt()))
                .thenReturn(Map.of("дорога", 2));

        applicationService.go(config);

        verify(jsonResultWriter)
                .write(any(AnalysisResult.class), eq(output));

        verify(consoleResultWriter, never())
                .write(any());
    }

    @Test
    void shouldIncludeReadErrorsInAnalysisResult() {
        Path output = Path.of("result.json");

        AnalysisConfig config = createJsonConfig(output, 10);

        ReadResult readResult = new ReadResult(
                Map.of(),
                Map.of(
                        Path.of("texts", "broken.txt"),
                        "Ошибка чтения"
                )
        );

        when(txtReader.read(any())).thenReturn(readResult);

        when(stopWordsReader.loadStopWords(any()))
                .thenReturn(Set.of());

        when(analyzer.analyze(any(), any(), anyInt()))
                .thenReturn(Map.of());

        applicationService.go(config);

        verify(jsonResultWriter)
                .write(analysisResultCaptor.capture(), eq(output));

        AnalysisResult result = analysisResultCaptor.getValue();

        assertEquals(1, result.errors().size());
        assertEquals("broken.txt", result.errors().getFirst().fileName());
        assertEquals("Ошибка чтения", result.errors().getFirst().message());
    }

    private AnalysisConfig createConsoleConfig(int top) {
        AnalysisConfig config = mock(AnalysisConfig.class);

        when(config.getDirectory()).thenReturn(Path.of("texts"));
        when(config.getStopWords()).thenReturn(Path.of("stopwords.txt"));
        when(config.getMinLength()).thenReturn(3);
        when(config.getTop()).thenReturn(top);
        when(config.getOutput()).thenReturn(null);

        return config;
    }

    private AnalysisConfig createJsonConfig(Path output, int top) {
        AnalysisConfig config = mock(AnalysisConfig.class);

        when(config.getDirectory()).thenReturn(Path.of("texts"));
        when(config.getStopWords()).thenReturn(Path.of("stopwords.txt"));
        when(config.getMinLength()).thenReturn(3);
        when(config.getTop()).thenReturn(top);
        when(config.getOutput()).thenReturn(output);

        return config;
    }
}