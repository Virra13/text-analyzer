package ru.virra.textanalyzer.model;

import java.util.List;

/**
 * Итоговый результат анализа текстов.
 *
 * @param analysisInfo параметры выполненного анализа
 * @param wordCount    список слов и количества их вхождений
 * @param errors       ошибки, возникшие при чтении отдельных файлов
 */

public record AnalysisResult(AnalysisInfo analysisInfo, List<WordCount> wordCount, List<FileReadError> errors) {}
