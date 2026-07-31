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
    private Path directory;
    private int minLength;
    private int top;
    private Path output;
    private Path stopWords;
    private int threads;
    private ExecutionMode mode;
}
