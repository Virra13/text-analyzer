package ru.virra.textanalyzer.processing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.virra.textanalyzer.analysis.processing.MultiThreadAnalysisProcessor;
import ru.virra.textanalyzer.analysis.analyzer.Analyzer;
import ru.virra.textanalyzer.analysis.input.TextReader;
import ru.virra.textanalyzer.analysis.model.ProcessingResult;
import ru.virra.textanalyzer.exception.FileProcessingException;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiThreadAnalysisProcessorTest {

    @Mock
    private TextReader textReader;

    @Mock
    private Analyzer analyzer;

    private ExecutorService analysisExecutor;
    private MultiThreadAnalysisProcessor processor;

    @BeforeEach
    void setUp() {
        analysisExecutor = Executors.newCachedThreadPool();
        processor = new MultiThreadAnalysisProcessor(textReader, analyzer, analysisExecutor);
    }

    @AfterEach
    void tearDown() {
        analysisExecutor.shutdownNow();
    }

    @Test
    void processReadsAndAnalyzesEachFile() {
        Path file1 = Path.of("first.txt");
        Path file2 = Path.of("second.txt");

        String text1 = "java spring java";
        String text2 = "java thread thread";

        Set<String> stopWords = Set.of();

        when(textReader.read(file1)).thenReturn(text1);
        when(textReader.read(file2)).thenReturn(text2);

        when(analyzer.analyze(List.of(text1), stopWords, 3))
                .thenReturn(Map.of("java", 2, "spring", 1));

        when(analyzer.analyze(List.of(text2), stopWords, 3))
                .thenReturn(Map.of("java", 1, "thread", 2));

        ProcessingResult result = processor.process(List.of(file1, file2), stopWords, 3, 2);

        assertEquals(Map.of("java", 3, "spring", 1, "thread", 2), result.wordCounts());
        assertEquals(2, result.processedFiles());
        assertEquals(Map.of(), result.readErrors());

        verify(textReader).read(file1);
        verify(textReader).read(file2);
        verify(analyzer).analyze(List.of(text1), stopWords, 3);
        verify(analyzer).analyze(List.of(text2), stopWords, 3);
    }

    @Test
    void processMergesWordCountsFromDifferentFiles() {
        Path file1 = Path.of("first.txt");
        Path file2 = Path.of("second.txt");

        String text1 = "java java";
        String text2 = "java java java";

        Set<String> stopWords = Set.of();

        when(textReader.read(file1)).thenReturn(text1);
        when(textReader.read(file2)).thenReturn(text2);

        when(analyzer.analyze(List.of(text1), stopWords, 3))
                .thenReturn(Map.of("java", 2));

        when(analyzer.analyze(List.of(text2), stopWords, 3))
                .thenReturn(Map.of("java", 3));

        ProcessingResult result = processor.process(List.of(file1, file2), stopWords, 3, 2);

        assertEquals(5, result.wordCounts().get("java"));
        assertEquals(2, result.processedFiles());
    }

    @Test
    void processWithEmptyFilesReturnsEmptyResult() {
        ProcessingResult result = processor.process(List.of(), Set.of(), 3, 2);

        assertEquals(Map.of(), result.wordCounts());
        assertEquals(Map.of(), result.readErrors());
        assertEquals(0, result.processedFiles());
    }

    @Test
    void shouldProcessFilesConcurrently() throws Exception {
        int fileCount = 101;
        int threads = 4;

        List<Path> files = new ArrayList<>();

        for (int i = 0; i < fileCount; i++) {
            files.add(Path.of(String.valueOf(i)));
        }

        CountDownLatch started = new CountDownLatch(threads);
        CountDownLatch release = new CountDownLatch(1);

        Set<Thread> workerThreads = ConcurrentHashMap.newKeySet();

        when(textReader.read(any(Path.class))).thenAnswer(invocation -> {

            workerThreads.add(Thread.currentThread());

            started.countDown();
            release.await();
            return "text";
        });

        when(analyzer.analyze(anyList(), anySet(), anyInt())).thenReturn(Map.of());

        ExecutorService testExecutor = Executors.newSingleThreadExecutor();

        try {
            Future<ProcessingResult> future = testExecutor.submit(
                    () -> processor.process( files, Set.of(),3, threads)
            );

            assertTrue(started.await(2, TimeUnit.SECONDS));
            assertEquals(threads, workerThreads.size());

            release.countDown();

            ProcessingResult result = future.get(2, TimeUnit.SECONDS);
            assertEquals(fileCount, result.processedFiles());

            verify(textReader, times(fileCount)).read(any(Path.class));

        } finally {
            release.countDown();
            testExecutor.shutdownNow();
        }
    }

    @Test
    void shouldContinueProcessingWhenSomeFileFails() {
        int fileCount = 90;
        int threads = 4;

        List<Path> files = new ArrayList<>();

        for (int i = 0; i < fileCount; i++) {
            files.add(Path.of(String.valueOf(i)));
        }

        when(textReader.read(any(Path.class)))
                .thenAnswer(invocation -> {
                    Path file = invocation.getArgument(0);

                    int number = Integer.parseInt(file.toString());

                    if (number % 3 == 0) {
                        throw new FileProcessingException("Read failed");
                    }

                    return "java";
                });

        when(analyzer.analyze(anyList(), anySet(), anyInt())).thenReturn(Map.of("java", 1));

        ProcessingResult result = processor.process(files, Set.of(),3, threads);

        assertEquals(60, result.processedFiles());
        assertEquals(30, result.readErrors().size());
        assertEquals(60, result.wordCounts().get("java"));

        verify(textReader, times(fileCount)).read(any(Path.class));
    }

    @Test
    void shouldMergeDifferentResultsDuringConcurrentProcessing() {
        int fileCount = 200;
        int threads = 8;

        List<Path> files = new ArrayList<>();

        for (int i = 1; i <= fileCount; i++) {
            files.add(Path.of(String.valueOf(i)));
        }

        when(textReader.read(any(Path.class))).thenAnswer(invocation -> {
            Path file = invocation.getArgument(0);
            return file.toString();
        });

        when(analyzer.analyze(anyList(), anySet(), anyInt()))
                .thenAnswer(invocation -> {
                    List<String> texts = invocation.getArgument(0);

                    int number = Integer.parseInt(texts.get(0));

                    Map<String, Integer> result = new HashMap<>();

                    if (number % 2 == 0) {
                        result.put("java", 1);
                    }

                    if (number % 3 == 0) {
                        result.put("spring", 1);
                    }

                    if (number % 5 == 0) {
                        result.put("hello", 1);
                    }

                    if (number % 7 == 0) {
                        result.merge("spring", 2, Integer::sum);
                    }

                    return result;
                });

        ProcessingResult result = processor.process(files, Set.of(),3, threads);

        assertEquals(fileCount, result.processedFiles());
        assertEquals(0, result.readErrors().size());

        assertEquals(100, result.wordCounts().get("java"));
        assertEquals(122, result.wordCounts().get("spring"));
        assertEquals(40, result.wordCounts().get("hello"));

        verify(textReader, times(fileCount)).read(any(Path.class));
    }

    @Timeout(5)
    @Test
    void shouldStopProcessingWhenUnexpectedErrorOccurs() throws Exception {
        Path failedFile = Path.of("0");
        Path runningFile = Path.of("1");
        Path notStartedFile = Path.of("2");

        CountDownLatch runningFileStarted = new CountDownLatch(1);
        CountDownLatch runningFileInterrupted = new CountDownLatch(1);

        when(textReader.read(failedFile)).thenAnswer(invocation -> {
            runningFileStarted.await();
            return "error";
        });

        when(textReader.read(runningFile)).thenAnswer(invocation -> {
            runningFileStarted.countDown();

            try {
                Thread.sleep(10_000);
                return "java";

            } catch (InterruptedException e) {
                runningFileInterrupted.countDown();
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        when(analyzer.analyze(List.of("error"), Set.of(), 3))
                .thenThrow(new IllegalStateException("Unexpected error"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> processor.process(
                        List.of(failedFile, runningFile, notStartedFile),
                        Set.of(),
                        3,
                        2
                )
        );

        assertEquals("Unexpected error", exception.getMessage());
        assertTrue(runningFileInterrupted.await(2, TimeUnit.SECONDS));

        verify(textReader, never()).read(notStartedFile);
    }

    @Test
    void shouldProcessAllFilesWithSingleWorker() {
        int fileCount = 20;

        List<Path> files = new ArrayList<>();

        for (int i = 0; i < fileCount; i++) {
            files.add(Path.of(String.valueOf(i)));
        }

        when(textReader.read(any(Path.class))).thenReturn("java");

        when(analyzer.analyze(anyList(), anySet(), anyInt())).thenReturn(Map.of("java", 1));

        ProcessingResult result = processor.process(
                files,
                Set.of(),
                3,
                1
        );

        assertEquals(fileCount, result.processedFiles());
        assertEquals(0, result.readErrors().size());
        assertEquals(fileCount,result.wordCounts().get("java"));
        verify(textReader, times(fileCount)).read(any(Path.class));
    }

    @Test
    void shouldReturnOnlyErrorsWhenAllFilesFail() {
        int fileCount = 20;
        int threads = 4;

        List<Path> files = new ArrayList<>();

        for (int i = 0; i < fileCount; i++) {
            files.add(Path.of(String.valueOf(i)));
        }

        when(textReader.read(any(Path.class))).thenThrow(new FileProcessingException("Read failed"));

        ProcessingResult result = processor.process(files,Set.of(), 3, threads);

        assertEquals(0, result.processedFiles());
        assertTrue(result.wordCounts().isEmpty());
        assertEquals(fileCount, result.readErrors().size());

        verify(textReader, times(fileCount)).read(any(Path.class));
        verifyNoInteractions(analyzer);
    }

    @Test
    void shouldProcessOtherFilesWhileFirstFileIsBlocked() throws Exception {
        int fileCount = 6;
        int threads = 2;

        Path slowFile = Path.of("0");

        List<Path> files = new ArrayList<>();

        for (int i = 0; i < fileCount; i++) {
            files.add(Path.of(String.valueOf(i)));
        }

        CountDownLatch slowStarted = new CountDownLatch(1);
        CountDownLatch releaseSlow = new CountDownLatch(1);
        CountDownLatch otherFilesProcessed = new CountDownLatch(fileCount - 1);

        when(textReader.read(any(Path.class))).thenAnswer(invocation -> {
                    Path file = invocation.getArgument(0);

                    if (file.equals(slowFile)) {
                        slowStarted.countDown();
                        releaseSlow.await();
                    }

                    return file.toString();
                });

        when(analyzer.analyze(anyList(), anySet(), anyInt())).thenAnswer(invocation -> {
                    List<String> texts = invocation.getArgument(0);

                    if (!texts.get(0).equals("0")) {
                        otherFilesProcessed.countDown();
                    }

                    return Map.of("java", 1);
                });

        ExecutorService testExecutor = Executors.newSingleThreadExecutor();

        try {
            Future<ProcessingResult> future = testExecutor.submit(
                    () -> processor.process(files, Set.of(),3, threads)
            );

            assertTrue(slowStarted.await(2, TimeUnit.SECONDS));

            assertTrue(otherFilesProcessed.await(2, TimeUnit.SECONDS));

            releaseSlow.countDown();

            ProcessingResult result = future.get(2, TimeUnit.SECONDS);

            assertEquals(fileCount, result.processedFiles());

        } finally {
            releaseSlow.countDown();
            testExecutor.shutdownNow();
        }
    }
}