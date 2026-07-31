package ru.virra.textanalyzer.input;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.virra.textanalyzer.exception.FileProcessingException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileTextReaderTest {

    private final FileTextReader reader = new FileTextReader();

    @TempDir
    Path tempDir;

    @Test
    void readReturnsFileContent() throws IOException {
        Path file = tempDir.resolve("text.txt");
        Files.writeString(file, "Hello Java", StandardCharsets.UTF_8);

        String result = reader.read(file);

        assertEquals("Hello Java", result);
    }

    @Test
    void readReturnsMultilineContent() throws IOException {
        Path file = tempDir.resolve("text.txt");
        String content = "First line\nSecond line\nThird line";

        Files.writeString(file, content, StandardCharsets.UTF_8);

        String result = reader.read(file);

        assertEquals(content, result);
    }

    @Test
    void readReturnsUnicodeContent() throws IOException {
        Path file = tempDir.resolve("text.txt");
        String content = "Привет, мир! Татарстан — республика.";

        Files.writeString(file, content, StandardCharsets.UTF_8);

        String result = reader.read(file);

        assertEquals(content, result);
    }

    @Test
    void readEmptyFileReturnsEmptyString() throws IOException {
        Path file = Files.createFile(tempDir.resolve("empty.txt"));

        String result = reader.read(file);

        assertEquals("", result);
    }

    @Test
    void readThrowsExceptionWhenFileDoesNotExist() {
        Path file = tempDir.resolve("missing.txt");

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> reader.read(file)
        );

        assertTrue(exception.getMessage().contains("I/O error"));
    }
}
