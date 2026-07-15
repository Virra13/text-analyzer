package ru.virra.textanalyzer.input;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.FileProcessingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Читает файл со стоп-словами и формирует набор слов,
 * которые должны быть исключены из анализа.
 *
 * <p>Слова извлекаются с помощью регулярного выражения
 * и приводятся к нижнему регистру.</p>
 */
@Slf4j
@Component
public class StopWordsReader {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\p{L}+(?:[-_'’]\\p{L}+)*");

    /**
     * Загружает стоп-слова из указанного файла.
     *
     * <p>Если путь не указан, возвращается пустой набор.</p>
     *
     * @param path путь к файлу со стоп-словами или {@code null},
     *             если стоп-слова не используются
     * @return набор стоп-слов в нижнем регистре
     * @throws FileProcessingException если файл отсутствует,
     *                                 не является обычным файлом,
     *                                 недоступен для чтения
     *                                 или произошла ошибка чтения
     */
    public Set<String> loadStopWords(Path path) {

        if (path == null) {
            return Set.of();
        }

        log.info("Loading stopwords from file: {}", path);

        if (!Files.exists(path)){
            throw new FileProcessingException("Error: path to stopwords does not exist: " + path);
        }

        if (!Files.isRegularFile(path)) {
            throw new FileProcessingException("Error: path to stopwords is not a regular file: " + path);
        }

        if (!Files.isReadable(path)) {
            throw new FileProcessingException("Error: file with stopwords is not readable: " + path);
        }

        String text;
        Set<String> words = new HashSet<>();
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileProcessingException("Error: failed to read stopwords file '" + path + "': " + e.getMessage());
        }

        Matcher matcher = WORD_PATTERN.matcher(text);
        while (matcher.find()) {
            String word = matcher.group().toLowerCase(Locale.ROOT);
            words.add(word);
        }

        log.info("Loaded {} stopwords from file: {}", words.size(), path);
        return words;
    }
}
