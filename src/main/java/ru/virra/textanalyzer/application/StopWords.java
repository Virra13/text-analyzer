package ru.virra.textanalyzer.application;

import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.InvalidArgumentsException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StopWords {

    private final Pattern pattern = Pattern.compile("\\p{L}+(?:[-_'’]\\p{L}+)*");

    public Set<String> loadStopWords(Path path) {

        if (path == null) {
            return Set.of();
        }

        if (!Files.exists(path)){
            throw new InvalidArgumentsException("Error: path does not exist: " + path);
        }

        if (!Files.isRegularFile(path)) {
            throw new InvalidArgumentsException("Error: path is not a regular file: " + path);
        }

        if (!Files.isReadable(path)) {
            throw new InvalidArgumentsException("Error: file is not readable: " + path);
        }

        String text;
        Set<String> words = new HashSet<>();
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidArgumentsException(
                    "Error: failed to read stopwords file '" + path + "': " + e.getMessage()
            );
        }

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String word = matcher.group().toLowerCase(Locale.ROOT);
            words.add(word);
        }

        return words;
    }
}
