package ru.virra.textanalyzer.analysis.application;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

/**
 * Конфигурация запуска анализа текстов.
 *
 * <p>Содержит параметры, необходимые для выполнения анализа:
 * директорию с текстовыми файлами, ограничения анализа,
 * параметры вывода, стоп-слова и настройки режима обработки.</p>
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
