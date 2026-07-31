package ru.virra.textanalyzer.processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.virra.textanalyzer.analyzer.Analyzer;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.input.TextReader;
import ru.virra.textanalyzer.model.ProcessingResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleThreadAnalysisProcessorTest {

    @Mock
    private TextReader textReader;

    @Mock
    private Analyzer analyzer;

    @InjectMocks
    private SingleThreadAnalysisProcessor processor;

    @Test
    void processReadsAnalyzesAndMergesFiles() {
        Path file1 = Path.of("first.txt");
        Path file2 = Path.of("second.txt");

        String text1 = "java spring java";
        String text2 = "java thread thread";

        Set<String> stopWords = Set.of();

        when(textReader.read(file1)).thenReturn(text1);
        when(textReader.read(file2)).thenReturn(text2);

        when(analyzer.analyze(List.of(text1), stopWords, 3)).thenReturn(Map.of("java", 2, "spring", 1));
        when(analyzer.analyze(List.of(text2), stopWords, 3)).thenReturn(Map.of("java", 1, "thread", 2));

        ProcessingResult result = processor.process(List.of(file1, file2), stopWords, 3, 2);

        assertEquals(Map.of("java", 3, "spring", 1, "thread", 2), result.wordCounts());
        assertEquals(Map.of(), result.readErrors());
        assertEquals(2, result.processedFiles());

        verify(textReader).read(file1);
        verify(textReader).read(file2);
        verify(analyzer).analyze(List.of(text1), stopWords, 3);
        verify(analyzer).analyze(List.of(text2), stopWords, 3);
    }

    @Test
    void processAddsReadErrorAndContinuesProcessing() {
        Path brokenFile = Path.of("broken.txt");
        Path validFile = Path.of("valid.txt");

        String text = "java spring";

        Set<String> stopWords = Set.of();

        when(textReader.read(brokenFile)).thenThrow(new FileProcessingException("Access denied"));
        when(textReader.read(validFile)).thenReturn(text);

        when(analyzer.analyze(List.of(text), stopWords, 3)).thenReturn(Map.of("java", 1, "spring", 1));

        ProcessingResult result = processor.process(List.of(brokenFile, validFile), stopWords, 3, 2);

        assertEquals(Map.of("java", 1, "spring", 1), result.wordCounts());
        assertEquals(Map.of(brokenFile, "Access denied"), result.readErrors());
        assertEquals(1, result.processedFiles());

        verify(textReader).read(brokenFile);
        verify(textReader).read(validFile);
        verify(analyzer).analyze(List.of(text), stopWords, 3);
    }

    @Test
    void processWithEmptyFilesReturnsEmptyResult() {
        ProcessingResult result = processor.process(List.of(), Set.of(), 3, 2);

        assertEquals(Map.of(), result.wordCounts());
        assertEquals(Map.of(), result.readErrors());
        assertEquals(0, result.processedFiles());
    }
}