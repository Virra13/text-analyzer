package ru.virra.textanalyzer.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.virra.textanalyzer.input.DirectoryScanner;
import ru.virra.textanalyzer.input.StopWordsReader;
import ru.virra.textanalyzer.model.AnalysisResult;
import ru.virra.textanalyzer.model.ProcessingResult;
import ru.virra.textanalyzer.model.WordCount;
import ru.virra.textanalyzer.output.ConsoleResultWriter;
import ru.virra.textanalyzer.output.JsonResultWriter;
import ru.virra.textanalyzer.processing.AnalysisProcessor;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private DirectoryScanner directoryScanner;

    @Mock
    private AnalysisProcessor processor;

    @Mock
    private StopWordsReader stopWordsReader;

    @Mock
    private ConsoleResultWriter consoleResultWriter;

    @Mock
    private JsonResultWriter jsonResultWriter;

    @Captor
    private ArgumentCaptor<AnalysisResult> analysisResultCaptor;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        Map<String, AnalysisProcessor> processors = Map.of(
                "single", processor,
                "multi", processor
        );

        applicationService = new ApplicationService(
                directoryScanner,
                processors,
                stopWordsReader,
                consoleResultWriter,
                jsonResultWriter
        );
    }

    @Test
    void shouldPassDataToProcessor() {
        Path directory = Path.of("texts");
        Path stopWordsPath = Path.of("stopwords.txt");

        AnalysisConfig config = createConsoleConfig(4, 10);

        List<Path> files = List.of(
                Path.of("texts", "first.txt"),
                Path.of("texts", "second.txt")
        );

        Set<String> stopWords = Set.of("первый", "второй");

        when(directoryScanner.scan(directory)).thenReturn(files);
        when(stopWordsReader.loadStopWords(stopWordsPath)).thenReturn(stopWords);
        when(processor.process(files, stopWords, 4, 2)).thenReturn(
                new ProcessingResult(Map.of(), Map.of(), 2)
        );

        applicationService.go(config);

        verify(directoryScanner).scan(directory);
        verify(stopWordsReader).loadStopWords(stopWordsPath);
        verify(processor).process(files, stopWords, 4, 2);
    }

    @Test
    void shouldSortWordsByCountDescending() {
        AnalysisConfig config = createConsoleConfig(3, 10);

        List<Path> files = List.of(Path.of("texts", "text.txt"));

        when(directoryScanner.scan(any())).thenReturn(files);
        when(stopWordsReader.loadStopWords(any())).thenReturn(Set.of());
        when(processor.process(any(), any(), anyInt(), anyInt())).thenReturn(
                new ProcessingResult(
                        Map.of(
                                "река", 2,
                                "лес", 5,
                                "ветер", 3
                        ),
                        Map.of(),
                        1
                )
        );

        applicationService.go(config);

        verify(consoleResultWriter).write(analysisResultCaptor.capture());

        List<WordCount> result = analysisResultCaptor.getValue().wordCount();

        assertEquals("лес", result.get(0).word());
        assertEquals(5, result.get(0).count());

        assertEquals("ветер", result.get(1).word());
        assertEquals(3, result.get(1).count());

        assertEquals("река", result.get(2).word());
        assertEquals(2, result.get(2).count());
    }

    @Test
    void shouldSortAlphabeticallyWhenCountsAreEqual() {
        AnalysisConfig config = createConsoleConfig(3, 10);

        when(directoryScanner.scan(any())).thenReturn(
                List.of(Path.of("texts", "text.txt"))
        );

        when(stopWordsReader.loadStopWords(any())).thenReturn(Set.of());

        when(processor.process(any(), any(), anyInt(), anyInt())).thenReturn(
                new ProcessingResult(
                        Map.of(
                                "ветер", 3,
                                "берег", 3,
                                "река", 3
                        ),
                        Map.of(),
                        1
                )
        );

        applicationService.go(config);

        verify(consoleResultWriter).write(analysisResultCaptor.capture());

        List<WordCount> result = analysisResultCaptor.getValue().wordCount();

        assertEquals("берег", result.get(0).word());
        assertEquals("ветер", result.get(1).word());
        assertEquals("река", result.get(2).word());
    }

    @Test
    void shouldLimitResultByTop() {
        AnalysisConfig config = createConsoleConfig(3, 2);

        when(directoryScanner.scan(any())).thenReturn(
                List.of(Path.of("texts", "text.txt"))
        );

        when(stopWordsReader.loadStopWords(any())).thenReturn(Set.of());

        when(processor.process(any(), any(), anyInt(), anyInt())).thenReturn(
                new ProcessingResult(
                        Map.of(
                                "город", 7,
                                "улица", 5,
                                "машина", 4,
                                "окно", 2
                        ),
                        Map.of(),
                        1
                )
        );

        applicationService.go(config);

        verify(consoleResultWriter).write(analysisResultCaptor.capture());

        List<WordCount> result = analysisResultCaptor.getValue().wordCount();

        assertEquals(2, result.size());
        assertEquals("город", result.get(0).word());
        assertEquals("улица", result.get(1).word());
    }

    @Test
    void shouldUseConsoleWriterWhenOutputIsNull() {
        AnalysisConfig config = createConsoleConfig(3, 10);

        when(directoryScanner.scan(any())).thenReturn(
                List.of(Path.of("texts", "text.txt"))
        );

        when(stopWordsReader.loadStopWords(any())).thenReturn(Set.of());

        when(processor.process(any(), any(), anyInt(), anyInt())).thenReturn(
                new ProcessingResult(
                        Map.of("слово", 1),
                        Map.of(),
                        1
                )
        );

        applicationService.go(config);

        verify(consoleResultWriter).write(any(AnalysisResult.class));
        verify(jsonResultWriter, never()).write(any(), any());
    }

    @Test
    void shouldUseJsonWriterWhenOutputIsSpecified() {
        Path output = Path.of("result.json");

        AnalysisConfig config = createJsonConfig(output, 3, 10);

        when(directoryScanner.scan(any())).thenReturn(
                List.of(Path.of("texts", "text.txt"))
        );

        when(stopWordsReader.loadStopWords(any())).thenReturn(Set.of());

        when(processor.process(any(), any(), anyInt(), anyInt())).thenReturn(
                new ProcessingResult(
                        Map.of("дорога", 2),
                        Map.of(),
                        1
                )
        );

        applicationService.go(config);

        verify(jsonResultWriter).write(any(AnalysisResult.class), eq(output));
        verify(consoleResultWriter, never()).write(any(AnalysisResult.class));
    }

    @Test
    void shouldIncludeReadErrorsInAnalysisResult() {
        Path output = Path.of("result.json");

        AnalysisConfig config = createJsonConfig(output, 3, 10);

        List<Path> files = List.of(
                Path.of("texts", "broken.txt")
        );

        when(directoryScanner.scan(any())).thenReturn(files);
        when(stopWordsReader.loadStopWords(any())).thenReturn(Set.of());

        when(processor.process(any(), any(), anyInt(), anyInt())).thenReturn(
                new ProcessingResult(
                        Map.of(),
                        Map.of(
                                Path.of("texts", "broken.txt"),
                                "Ошибка чтения"
                        ),
                        0
                )
        );

        applicationService.go(config);

        verify(jsonResultWriter).write(analysisResultCaptor.capture(), eq(output));

        AnalysisResult result = analysisResultCaptor.getValue();

        assertEquals(1, result.errors().size());
        assertEquals("broken.txt", result.errors().getFirst().fileName());
        assertEquals("Ошибка чтения", result.errors().getFirst().message());
    }

    @Test
    void shouldIncludeProcessedFilesInAnalysisInfo() {
        AnalysisConfig config = createConsoleConfig(3, 10);

        List<Path> files = List.of(
                Path.of("texts", "first.txt"),
                Path.of("texts", "second.txt")
        );

        when(directoryScanner.scan(any())).thenReturn(files);
        when(stopWordsReader.loadStopWords(any())).thenReturn(Set.of());

        when(processor.process(any(), any(), anyInt(), anyInt())).thenReturn(
                new ProcessingResult(
                        Map.of("java", 2),
                        Map.of(),
                        2
                )
        );

        applicationService.go(config);

        verify(consoleResultWriter).write(analysisResultCaptor.capture());

        AnalysisResult result = analysisResultCaptor.getValue();

        assertEquals(2, result.analysisInfo().processedFiles());
        assertEquals(ExecutionMode.SINGLE, result.analysisInfo().mode());
        assertEquals(2, result.analysisInfo().threads());
    }

    private AnalysisConfig createConsoleConfig(int minLength, int top) {
        AnalysisConfig config = mock(AnalysisConfig.class);

        when(config.getDirectory()).thenReturn(Path.of("texts"));
        when(config.getStopWords()).thenReturn(Path.of("stopwords.txt"));
        when(config.getMinLength()).thenReturn(minLength);
        when(config.getTop()).thenReturn(top);
        when(config.getOutput()).thenReturn(null);
        when(config.getMode()).thenReturn(ExecutionMode.SINGLE);
        when(config.getThreads()).thenReturn(2);

        return config;
    }

    private AnalysisConfig createJsonConfig(Path output, int minLength, int top) {
        AnalysisConfig config = mock(AnalysisConfig.class);

        when(config.getDirectory()).thenReturn(Path.of("texts"));
        when(config.getStopWords()).thenReturn(Path.of("stopwords.txt"));
        when(config.getMinLength()).thenReturn(minLength);
        when(config.getTop()).thenReturn(top);
        when(config.getOutput()).thenReturn(output);
        when(config.getMode()).thenReturn(ExecutionMode.SINGLE);
        when(config.getThreads()).thenReturn(2);

        return config;
    }
}