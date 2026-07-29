package ru.virra.textanalyzer.input;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.virra.textanalyzer.exception.FileProcessingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StopWordsReaderTest {

    private StopWordsReader reader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        reader = new StopWordsReader();
    }

    @Test
    void shouldReturnEmptySetWhenPathIsNull() {
        Set<String> result = reader.loadStopWords(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReadStopWordsFromFile() throws IOException {
        Path file = tempDir.resolve("stopwords.txt");

        Files.writeString(
                file,
                "и в на после перед",
                StandardCharsets.UTF_8
        );

        Set<String> result = reader.loadStopWords(file);

        assertEquals(
                Set.of("и", "в", "на", "после", "перед"),
                result
        );
    }

    @Test
    void shouldConvertWordsToLowerCase() throws IOException {
        Path file = tempDir.resolve("stopwords.txt");

        Files.writeString(
                file,
                "После ПОСЛЕ после Перед ПЕРЕД",
                StandardCharsets.UTF_8
        );

        Set<String> result = reader.loadStopWords(file);

        assertEquals(
                Set.of("после", "перед"),
                result
        );
    }

    @Test
    void shouldRemoveDuplicateWords() throws IOException {
        Path file = tempDir.resolve("stopwords.txt");

        Files.writeString(
                file,
                "дорога ветер дорога лес ветер дорога",
                StandardCharsets.UTF_8
        );

        Set<String> result = reader.loadStopWords(file);

        assertEquals(
                Set.of("дорога", "ветер", "лес"),
                result
        );
    }

    @Test
    void shouldRecognizeWordsSeparatedByPunctuation() throws IOException {
        Path file = tempDir.resolve("stopwords.txt");

        Files.writeString(
                file,
                "после, перед; около. между! через?",
                StandardCharsets.UTF_8
        );

        Set<String> result = reader.loadStopWords(file);

        assertEquals(
                Set.of("после", "перед", "около", "между", "через"),
                result
        );
    }

    @Test
    void shouldRecognizeWordsWithHyphenAndApostrophe() throws IOException {
        Path file = tempDir.resolve("stopwords.txt");

        Files.writeString(
                file,
                "когда-нибудь из-за чьё-то тестовый_режим",
                StandardCharsets.UTF_8
        );

        Set<String> result = reader.loadStopWords(file);

        assertEquals(
                Set.of(
                        "когда-нибудь",
                        "из-за",
                        "чьё-то",
                        "тестовый_режим"
                ),
                result
        );
    }

    @Test
    void shouldIgnoreNumbers() throws IOException {
        Path file = tempDir.resolve("stopwords.txt");

        Files.writeString(
                file,
                "123 456 после 789 перед",
                StandardCharsets.UTF_8
        );

        Set<String> result = reader.loadStopWords(file);

        assertEquals(
                Set.of("после", "перед"),
                result
        );
    }

    @Test
    void shouldReturnEmptySetForEmptyFile() throws IOException {
        Path file = tempDir.resolve("stopwords.txt");

        Files.writeString(
                file,
                "",
                StandardCharsets.UTF_8
        );

        Set<String> result = reader.loadStopWords(file);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptySetWhenFileContainsNoWords() throws IOException {
        Path file = tempDir.resolve("stopwords.txt");

        Files.writeString(
                file,
                "123 456 !!! ??? ---",
                StandardCharsets.UTF_8
        );

        Set<String> result = reader.loadStopWords(file);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenFileDoesNotExist() {
        Path file = tempDir.resolve("missing.txt");

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> reader.loadStopWords(file)
        );

        assertEquals(
                "Error: path to stopwords does not exist: " + file,
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenPathIsDirectory() {
        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> reader.loadStopWords(tempDir)
        );

        assertEquals(
                "Error: path to stopwords is not a regular file: " + tempDir,
                exception.getMessage()
        );
    }
}