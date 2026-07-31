package ru.virra.textanalyzer.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analyzer.Analyzer;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component("multi")
@RequiredArgsConstructor
public class MultiThreadAnalysisProcessor implements AnalysisProcessor{
    private final Analyzer analyzer;

    @Override
    public Map<String, Integer> process(Collection<String> texts, Set<String> stopWords, int minLength, int threads) {
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            List<Future<Map<String, Integer>>> futures = new ArrayList<>();

            for (String text : texts) {
                Future<Map<String, Integer>> future = executor.submit(
                        () -> analyzer.analyze(
                                List.of(text),
                                stopWords,
                                minLength
                        )
                );

                futures.add(future);
            }

            Map<String, Integer> result = new HashMap<>();

            for (Future<Map<String, Integer>> future : futures) {
                Map<String, Integer> localResult = future.get();

                for (Map.Entry<String, Integer> entry : localResult.entrySet()) {
                    result.merge(
                            entry.getKey(),
                            entry.getValue(),
                            Integer::sum
                    );
                }
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Text analysis was interrupted", e);

        } catch (ExecutionException e) {
            throw new RuntimeException("Error during text analysis", e);

        }
    }
}