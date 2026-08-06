package ru.virra.textanalyzer.analysis.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.FileProcessingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Реализация {@link TextReader}, читающая отдельные текстовые файлы
 * в кодировке UTF-8.
 */
@Slf4j
@Component
public class FileTextReader implements TextReader {

    /**
     * Читает содержимое указанного файла в кодировке UTF-8.
     *
     * @param path путь к файлу
     * @return содержимое файла
     * @throws FileProcessingException если файл недоступен или произошла ошибка ввода-вывода
     */
    @Override
    public String read(Path path) {

        log.debug("Reading file: {}", path);

        try {
            String text = Files.readString(path, StandardCharsets.UTF_8);
            log.debug("File read successfully: {}", path);
            return text;

        } catch (AccessDeniedException e) {
            log.warn("Access denied while reading file: {}", path);
            throw new FileProcessingException(
                    "Access denied while reading file: " + path
            );

        } catch (IOException e) {
            log.error("I/O error while reading file: {}", path, e);
            throw new FileProcessingException(
                    "I/O error while reading file: " + path
            );
        }
    }
}