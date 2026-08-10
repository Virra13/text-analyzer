package ru.virra.textanalyzer.analysis.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.FileProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Выполняет поиск текстовых файлов в указанной директории.
 */
@Slf4j
@Component
public class DirectoryScanner {

    /**
     * Возвращает обычные файлы с расширением .txt из указанной директории.
     *
     * @param path путь к директории
     * @return список найденных текстовых файлов
     * @throws FileProcessingException если директория отсутствует,
     * недоступна для чтения или указанный путь не является директорией
     */
    public List<Path> scan(Path path) {

        validateDirectory(path);

        try (Stream<Path> files = Files.list(path)) {

            List<Path> textFiles = files
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName()
                            .toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".txt"))
                    .toList();

            log.info("Found {} text files in directory: {}", textFiles.size(), path);

            return textFiles;

        } catch (IOException e) {
            log.error("Failed to list directory: {}", path, e);

            throw new FileProcessingException("Error: failed to read directory: " + path);
        }
    }

    private void validateDirectory(Path path) {

        if (!Files.exists(path)) {
            throw new FileProcessingException("Error: directory does not exist: " + path);
        }

        if (!Files.isDirectory(path)) {
            throw new FileProcessingException("Error: path is not a directory: " + path);
        }

        if (!Files.isReadable(path)) {
            throw new FileProcessingException("Error: directory is not readable: " + path);
        }
    }
}