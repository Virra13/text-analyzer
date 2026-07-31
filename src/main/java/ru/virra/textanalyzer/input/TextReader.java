package ru.virra.textanalyzer.input;

import ru.virra.textanalyzer.model.ReadResult;

import java.nio.file.Path;

/**
 * Интерфейс чтения текстовых файлов из указанной папки.
 */
public interface TextReader {

    /**
     * Читает содержимое одного текстового файла.
     *
     * @param path путь к текстовому файлу
     * @return содержимое файла
     */
    String read(Path path);

}
