package ru.virra.textanalyzer.model;

import java.nio.file.Path;
import java.util.Map;

/**
 * Результат обработки набора файлов.
 *
 * @param wordCounts объединённая частота слов
 * @param readErrors ошибки обработки отдельных файлов
 * @param processedFiles количество успешно обработанных файлов
 */
public record ProcessingResult(
        Map<String, Integer> wordCounts,
        Map<Path, String> readErrors,
        int processedFiles
) {
}