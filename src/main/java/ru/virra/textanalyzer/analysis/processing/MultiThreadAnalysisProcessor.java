package ru.virra.textanalyzer.analysis.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analysis.analyzer.Analyzer;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.analysis.input.TextReader;
import ru.virra.textanalyzer.analysis.model.ProcessingResult;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

/**
 * Выполняет анализ текстовых файлов в многопоточном режиме.
 *
 * <p>Для каждого файла создаётся отдельная задача, выполняемая через общий
 * пул потоков. Количество одновременно выполняемых задач одного анализа
 * ограничивается параметром {@code threads}. Результаты отдельных файлов
 * объединяются в управляющем потоке после завершения задач.</p>
 */
@Slf4j
@Component("multi")
@RequiredArgsConstructor
public class MultiThreadAnalysisProcessor implements AnalysisProcessor {

    private final TextReader textReader;
    private final Analyzer analyzer;
    private final ExecutorService analysisExecutor;

    /**
     * Выполняет параллельную обработку переданных файлов.
     *
     * <p>Запускает не более {@code threads} задач одновременно, получает
     * результаты по мере их завершения и объединяет их в общий результат.
     * Ошибки обработки отдельных файлов сохраняются отдельно и не прерывают
     * обработку остальных файлов.</p>
     *
     * @param files файлы для обработки
     * @param stopWords стоп-слова, исключаемые из анализа
     * @param minLength минимальная длина учитываемого слова
     * @param threads максимальное количество одновременно выполняемых задач
     * @return объединённый результат обработки файлов
     */
    @Override
    public ProcessingResult process(Collection<Path> files, Set<String> stopWords, int minLength, int threads) {
        log.info("Starting multi-threaded processing of {} files using {} workers", files.size(), threads);

        ProcessingSession session = new ProcessingSession(files, stopWords, minLength);

        try {
            session.start(threads);
            ProcessingResult result = session.collectResults();

            log.info("Multi-threaded processing completed. Processed: {}, errors: {}",
                    result.processedFiles(), result.readErrors().size());

            return result;

        } catch (InterruptedException e) {
            log.warn("Multi-threaded processing was interrupted");

            session.cancelTasks();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Text analysis was interrupted", e);

        } catch (ExecutionException e) {
            session.cancelTasks();
            Throwable cause = e.getCause();

            log.error("Worker task failed unexpectedly: {}", cause.getMessage());

            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            if (cause instanceof Error error) {
                throw error;
            }

            throw new RuntimeException("Error during text analysis", cause);

        } catch (RuntimeException e) {
            session.cancelTasks();
            throw e;
        }
    }

    /**
     * Выполняет чтение и анализ одного файла.
     *
     * <p>При ожидаемой ошибке обработки файла возвращает результат,
     * содержащий описание ошибки, не прерывая весь анализ.</p>
     *
     * @param file обрабатываемый файл
     * @param stopWords стоп-слова, исключаемые из анализа
     * @param minLength минимальная длина учитываемого слова
     * @return результат обработки одного файла
     */
    private FileProcessingResult processFile(Path file, Set<String> stopWords, int minLength) {
        log.debug("Processing file in worker thread: {}", file);

        try {
            String text = textReader.read(file);

            Map<String, Integer> result = analyzer.analyze(List.of(text), stopWords, minLength);
            log.debug("File processed successfully: {}", file);

            return new FileProcessingResult(file, result, null);

        } catch (FileProcessingException e) {
            log.warn("Failed to process file {}: {}", file, e.getMessage());
            String message = e.getMessage() != null ? e.getMessage() : "File processing failed";

            return new FileProcessingResult(file, Map.of(), message);
        }
    }

    /**
     * Добавляет частоты слов из локального результата в общий результат анализа.
     *
     * @param result общий результат анализа
     * @param localResult результат обработки отдельного файла
     */
    private void merge(Map<String, Integer> result, Map<String, Integer> localResult) {
        for (Map.Entry<String, Integer> entry : localResult.entrySet()) {
            result.merge(
                    entry.getKey(),
                    entry.getValue(),
                    Integer::sum
            );
        }
    }

    /**
     * Хранит состояние выполнения одного многопоточного анализа.
     *
     * <p>Управляет очередью файлов, отправленными задачами и получением
     * результатов через {@link CompletionService}. Состояние каждой сессии
     * независимо от других одновременно выполняемых анализов.</p>
     */
    private class ProcessingSession {

        private final CompletionService<FileProcessingResult> completionService;
        private final List<Future<FileProcessingResult>> submittedTasks;
        private final Iterator<Path> iterator;
        private final Set<String> stopWords;
        private final int minLength;
        private final int fileCount;

        private ProcessingSession(Collection<Path> files, Set<String> stopWords, int minLength) {
            this.completionService = new ExecutorCompletionService<>(analysisExecutor);
            this.submittedTasks = new ArrayList<>();
            this.iterator = files.iterator();
            this.stopWords = stopWords;
            this.minLength = minLength;
            this.fileCount = files.size();
        }

        /**
         * Запускает начальный набор задач обработки файлов.
         *
         * @param threads максимальное количество одновременно запущенных задач
         */
        private void start(int threads) {
            for (int i = 0; i < threads && iterator.hasNext(); i++) {
                submitNext();
            }
        }

        /**
         * Получает результаты задач по мере их завершения и формирует
         * общий результат анализа.
         *
         * @return объединённый результат обработки файлов
         * @throws InterruptedException если поток ожидания был прерван
         * @throws ExecutionException если задача завершилась неожиданной ошибкой
         */
        private ProcessingResult collectResults() throws InterruptedException, ExecutionException {
            Map<String, Integer> result = new HashMap<>();
            Map<Path, String> readErrors = new HashMap<>();

            int processedFiles = 0;

            for (int completedTasks = 0; completedTasks < fileCount; completedTasks++) {
                FileProcessingResult fileResult = completionService.take().get();

                if (fileResult.error() != null) {
                    readErrors.put(fileResult.file(), fileResult.error());

                } else {
                    merge(result, fileResult.wordCounts());
                    processedFiles++;
                }

                if (iterator.hasNext()) {
                    submitNext();
                }
            }

            return new ProcessingResult(result, readErrors, processedFiles);
        }

        /**
         * Отправляет следующий файл на обработку в общий пул потоков.
         */
        private void submitNext() {
            Path file = iterator.next();

            log.debug("Submitting file for processing: {}", file);

            Future<FileProcessingResult> future = completionService.submit(
                    () -> processFile(file, stopWords, minLength)
            );

            submittedTasks.add(future);
        }

        /**
         * Отменяет незавершённые задачи текущего анализа.
         */
        private void cancelTasks() {
            int cancelledTasks = 0;
            for (Future<FileProcessingResult> task : submittedTasks) {
                if (!task.isDone() && task.cancel(true)) {
                    cancelledTasks++;
                }
            }
            log.warn("Cancelled {} unfinished tasks", cancelledTasks);
        }
    }

    private record FileProcessingResult(Path file, Map<String, Integer> wordCounts, String error) {}
}