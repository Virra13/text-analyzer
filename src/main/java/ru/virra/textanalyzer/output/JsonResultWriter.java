package ru.virra.textanalyzer.output;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.model.AnalysisResult;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Записывает результат анализа текста в JSON-файл.
 *
 * <p>Перед записью проверяет корректность выходного пути,
 * доступность родительской директории и возможность записи
 * в существующий файл.</p>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JsonResultWriter {

    private final ObjectMapper objectMapper;

    /**
     * Сохраняет результат анализа в указанный JSON-файл.
     *
     * @param analysisResult результат анализа, который необходимо записать
     * @param path путь к выходному JSON-файлу
     * @throws FileProcessingException если выходной путь некорректен
     *                                 или запись файла невозможна
     */
    public void write(AnalysisResult analysisResult, Path path) {

        validate(path);
        File outputFile = path.toFile();
        objectMapper.writeValue(outputFile, analysisResult);
        log.info("Analysis result written successfully to: {}",path);
    }

    /**
     * Проверяет возможность записи результата по указанному пути.
     *
     * @param path путь к выходному файлу
     * @throws FileProcessingException если родительская директория
     *                                 отсутствует, не является директорией,
     *                                 недоступна для записи либо существующий
     *                                 выходной путь не является доступным файлом
     */
    private void validate(Path path) {

        Path parent = path.getParent();
        if (parent != null) {
            if (!Files.exists(parent)) {
                throw new FileProcessingException("Error: directory for output does not exist: " + parent);
            }
            if (!Files.isDirectory(parent)) {
                throw new FileProcessingException("Error: path for output is not a directory: " + parent);
            }
            if (!Files.isWritable(parent)) {
                throw new FileProcessingException("Error: directory for output is not writable: " + parent);
            }
        }

        if (Files.exists(path)) {
            if (!Files.isRegularFile(path)) {
                throw new FileProcessingException("Error: output path is not a regular file: " + path);
            }

            if (!Files.isWritable(path)) {
                throw new FileProcessingException("Error: path for output is not writable: " + path);
            }
        }
    }
}