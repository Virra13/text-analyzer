package ru.virra.textanalyzer.analysis.input;

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
