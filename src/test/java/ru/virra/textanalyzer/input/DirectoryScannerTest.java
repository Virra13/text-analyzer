package ru.virra.textanalyzer.input;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.virra.textanalyzer.analysis.input.DirectoryScanner;
import ru.virra.textanalyzer.exception.FileProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryScannerTest {

    private final DirectoryScanner scanner = new DirectoryScanner();

    @TempDir
    Path tempDir;

    @Test
    void scanReturnsTxtFiles() throws IOException {
        Path first = Files.createFile(tempDir.resolve("first.txt"));
        Path second = Files.createFile(tempDir.resolve("second.txt"));

        List<Path> result = scanner.scan(tempDir);

        assertEquals(2, result.size());
        assertTrue(result.contains(first));
        assertTrue(result.contains(second));
    }

    @Test
    void scanIgnoresNonTxtFiles() throws IOException {
        Path textFile = Files.createFile(tempDir.resolve("text.txt"));

        Files.createFile(tempDir.resolve("image.png"));
        Files.createFile(tempDir.resolve("data.json"));
        Files.createFile(tempDir.resolve("document.pdf"));

        List<Path> result = scanner.scan(tempDir);

        assertEquals(1, result.size());
        assertTrue(result.contains(textFile));
    }

    @Test
    void scanAcceptsTxtExtensionInDifferentCase() throws IOException {
        Path first = Files.createFile(tempDir.resolve("first.TXT"));
        Path second = Files.createFile(tempDir.resolve("second.TxT"));

        List<Path> result = scanner.scan(tempDir);

        assertEquals(2, result.size());
        assertTrue(result.contains(first));
        assertTrue(result.contains(second));
    }

    @Test
    void scanIgnoresDirectoriesWithTxtExtension() throws IOException {
        Files.createDirectory(tempDir.resolve("directory.txt"));

        List<Path> result = scanner.scan(tempDir);

        assertTrue(result.isEmpty());
    }

    @Test
    void scanEmptyDirectoryReturnsEmptyList() {
        List<Path> result = scanner.scan(tempDir);

        assertTrue(result.isEmpty());
    }

    @Test
    void scanThrowsExceptionWhenDirectoryDoesNotExist() {
        Path missing = tempDir.resolve("missing");

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> scanner.scan(missing)
        );

        assertTrue(exception.getMessage().contains("directory does not exist"));
    }

    @Test
    void scanThrowsExceptionWhenPathIsFile() throws IOException {
        Path file = Files.createFile(tempDir.resolve("file.txt"));

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> scanner.scan(file)
        );

        assertTrue(exception.getMessage().contains("path is not a directory"));
    }
}