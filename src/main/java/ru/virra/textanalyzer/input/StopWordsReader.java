package ru.virra.textanalyzer.input;

import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.exception.InvalidArgumentsException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StopWordsReader {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\p{L}+(?:[-_'’]\\p{L}+)*");

    public Set<String> loadStopWords(Path path) {

        if (path == null) {
            return Set.of();
        }

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

        return words;
    }
}
