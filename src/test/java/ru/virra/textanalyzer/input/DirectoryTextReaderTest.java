package ru.virra.textanalyzer.input;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.model.ReadResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class DirectoryTextReaderTest {
    private DirectoryTextReader reader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        reader = new DirectoryTextReader();
    }

    @Test
    void shouldReadTxtFiles() throws IOException {
        Path first = tempDir.resolve("first.txt");
        Path second = tempDir.resolve("second.txt");

        Files.writeString(first, "Первый текст", StandardCharsets.UTF_8);
        Files.writeString(second, "Второй текст", StandardCharsets.UTF_8);

        ReadResult result = reader.read(tempDir);

        assertEquals(2, result.texts().size());
        assertEquals("Первый текст", result.texts().get(first));
        assertEquals("Второй текст", result.texts().get(second));
        assertTrue(result.readErrors().isEmpty());
    }

    @Test
    void shouldIgnoreFilesWithOtherExtensions() throws IOException {
        Path txtFile = tempDir.resolve("article.txt");
        Path jsonFile = tempDir.resolve("data.json");
        Path javaFile = tempDir.resolve("Main.java");

        Files.writeString(txtFile, "Текстовый файл");
        Files.writeString(jsonFile, "{}");
        Files.writeString(javaFile, "class Main {}");

        ReadResult result = reader.read(tempDir);

        assertEquals(1, result.texts().size());
        assertEquals("Текстовый файл", result.texts().get(txtFile));

        assertFalse(result.texts().containsKey(jsonFile));
        assertFalse(result.texts().containsKey(javaFile));
    }

    @Test
    void shouldRecognizeTxtExtensionIgnoringCase() throws IOException {
        Path first = tempDir.resolve("first.TXT");
        Path second = tempDir.resolve("second.TxT");

        Files.writeString(first, "Первый");
        Files.writeString(second, "Второй");

        ReadResult result = reader.read(tempDir);

        assertEquals(2, result.texts().size());
        assertEquals("Первый", result.texts().get(first));
        assertEquals("Второй", result.texts().get(second));
    }

    @Test
    void shouldIgnoreDirectoriesWithTxtExtension() throws IOException {
        Path directory = tempDir.resolve("folder.txt");
        Files.createDirectory(directory);

        Path file = tempDir.resolve("text.txt");
        Files.writeString(file, "Обычный файл");

        ReadResult result = reader.read(tempDir);

        assertEquals(1, result.texts().size());
        assertEquals("Обычный файл", result.texts().get(file));
        assertFalse(result.texts().containsKey(directory));
    }

    @Test
    void shouldReturnEmptyResultForDirectoryWithoutTxtFiles() throws IOException {
        Files.writeString(tempDir.resolve("image.png"), "данные");
        Files.writeString(tempDir.resolve("data.csv"), "данные");

        ReadResult result = reader.read(tempDir);

        assertTrue(result.texts().isEmpty());
        assertTrue(result.readErrors().isEmpty());
    }

    @Test
    void shouldReturnEmptyResultForEmptyDirectory() {
        ReadResult result = reader.read(tempDir);

        assertTrue(result.texts().isEmpty());
        assertTrue(result.readErrors().isEmpty());
    }

    @Test
    void shouldReadUtf8TextCorrectly() throws IOException {
        Path file = tempDir.resolve("russian.txt");

        String text =
                "Ёжик шёл через берёзовую рощу. "
                        + "Потом он увидел реку.";

        Files.writeString(file, text, StandardCharsets.UTF_8);

        ReadResult result = reader.read(tempDir);

        assertEquals(text, result.texts().get(file));
        assertTrue(result.readErrors().isEmpty());
    }

    @Test
    void shouldThrowWhenDirectoryDoesNotExist() {
        Path missingDirectory = tempDir.resolve("missing");

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> reader.read(missingDirectory)
        );

        assertEquals(
                "Error: directory does not exist: " + missingDirectory,
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenPathIsFile() throws IOException {
        Path file = tempDir.resolve("text.txt");
        Files.writeString(file, "Текст");

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> reader.read(file)
        );

        assertEquals(
                "Error: path is not a directory: " + file,
                exception.getMessage()
        );
    }
}