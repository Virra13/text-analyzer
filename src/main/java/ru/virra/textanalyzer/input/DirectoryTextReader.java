package ru.virra.textanalyzer.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.model.ReadResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Реализация {@link TextReader}, выполняющая чтение текстовых файлов
 * из указанной директории.
 *
 * <p>Читает все файлы с расширением {@code .txt}, расположенные
 * непосредственно в заданной папке. Ошибки чтения отдельных файлов
 * не прерывают обработку остальных файлов и сохраняются
 * в результате чтения.</p>
 */
@Slf4j
@Component
public class DirectoryTextReader implements TextReader {

    /**
     * Читает все текстовые файлы из указанной директории.
     *
     * @param path путь к директории с текстовыми файлами
     * @return результат чтения, содержащий успешно прочитанные тексты
     *         и информацию об ошибках чтения отдельных файлов
     * @throws FileProcessingException если директория отсутствует,
     *                                 недоступна для чтения
     *                                 или не является директорией
     */
    public ReadResult read(Path path){

        log.info("Reading text files from directory: {}", path);
        validateDirectory(path);

        try (Stream<Path> list = Files.list(path)) {

            List<Path> listOfText = list
                    .filter(Files::isRegularFile)
                    .filter(x -> x.getFileName().toString()
                            .toLowerCase(Locale.ROOT)
                            .endsWith(".txt"))
                    .toList();

            log.info("Found {} text files in directory: {}", listOfText.size(), path);


            Map<Path, String> readErrors = new HashMap<>();
            Map<Path, String> texts = new HashMap<>();

            for (var file : listOfText) {
                log.debug("Reading file: {}", file);

                try {
                    texts.put(file, Files.readString(file, StandardCharsets.UTF_8));
                    log.debug("File read successfully: {}", file);
                } catch (AccessDeniedException e) {
                    log.warn("Access denied while reading file: {}", file);
                        readErrors.put(file, "Access denied");
                } catch (IOException e) {
                    log.error("I/O error while reading file: {}", file, e);
                    readErrors.put(file, "I/O error");
                }

            }

            log.info(
                    "Finished reading directory: {}. Successfully read: {}, errors: {}",
                    path,
                    texts.size(),
                    readErrors.size()
            );

            return new ReadResult(texts, readErrors);

        } catch (IOException e) {
            log.error("Failed to list directory: {}", path, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Проверяет корректность директории для чтения.
     *
     * @param path путь к директории
     * @throws FileProcessingException если директория отсутствует,
     *                                 недоступна для чтения
     *                                 или указанный путь
     *                                 не является директорией
     */
    private void validateDirectory(Path path) {
       if (!Files.exists(path)){
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