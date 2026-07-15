package ru.virra.textanalyzer.model;

import java.nio.file.Path;

/**
 * Параметры выполненного анализа.
 *
 * @param directory     папка с анализируемыми текстовыми файлами
 * @param minWordLength минимальная длина учитываемого слова
 * @param topCount      максимальное количество слов в результате
 */
public record AnalysisInfo(Path directory, int minWordLength, int topCount) {}
