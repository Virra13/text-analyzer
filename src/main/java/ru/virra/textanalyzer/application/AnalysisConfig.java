package ru.virra.textanalyzer.application;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

/**
 * Конфигурация запуска анализа текстов.
 *
 * <p>Содержит параметры, полученные и проверенные при разборе
 * аргументов командной строки.</p>
 */
@Getter
@Builder
public class AnalysisConfig {
    Path directory;
    int minLength;
    int top;
    Path output;
    Path stopWords;
}
