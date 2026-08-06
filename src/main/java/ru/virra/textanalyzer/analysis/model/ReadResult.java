package ru.virra.textanalyzer.analysis.model;

import java.nio.file.Path;
import java.util.Map;

/**
 * Результат чтения текстовых файлов.
 *
 * @param texts      успешно прочитанные тексты, где ключ — путь к файлу,
 *                   а значение — его содержимое
 * @param readErrors ошибки чтения файлов, где ключ — путь к файлу,
 *                   а значение — описание ошибки
 */
public record ReadResult (
        Map<Path, String> texts,
        Map<Path, String> readErrors) {

}
