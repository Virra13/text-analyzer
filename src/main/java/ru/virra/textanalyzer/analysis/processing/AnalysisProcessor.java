package ru.virra.textanalyzer.analysis.processing;

import ru.virra.textanalyzer.analysis.model.ProcessingResult;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

public interface AnalysisProcessor {

    /**
     * Обрабатывает набор текстовых файлов и объединяет результаты анализа.
     *
     * @param files набор файлов для обработки
     * @param stopWords стоп-слова, исключаемые из анализа
     * @param minLength минимальная длина учитываемого слова
     * @param threads количество потоков, доступных стратегии обработки
     * @return результат обработки файлов
     */
    ProcessingResult process(Collection<Path> files, Set<String> stopWords, int minLength, int threads);

}
