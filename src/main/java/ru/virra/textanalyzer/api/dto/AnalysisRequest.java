package ru.virra.textanalyzer.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;

/**
 * Параметры запуска анализа через REST API.
 *
 * @param directory директория с текстовыми файлами
 * @param minWordLength минимальная длина учитываемого слова
 * @param topCount количество наиболее частых слов в результате
 * @param mode режим обработки файлов
 * @param threads количество одновременно выполняемых задач в многопоточном режиме
 * @param stopWords путь к файлу со стоп-словами
 */
public record AnalysisRequest(
        @NotBlank
        String directory,

        @Min(1)
        int minWordLength,

        @Min(1)
        int topCount,

        @NotNull
        ExecutionMode mode,

        @Min(1)
        Integer threads,

        String stopWords
) {

        /**
         * Возвращает указанное количество потоков либо значение по умолчанию.
         *
         * @return количество потоков для выполнения анализа
         */
        public int resolvedThreads() {
                return threads != null ? threads : 2;
        }

}
