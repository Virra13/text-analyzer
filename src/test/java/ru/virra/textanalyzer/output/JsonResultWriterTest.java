package ru.virra.textanalyzer.output;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.model.AnalysisResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonResultWriterTest {

    @Mock
    private ObjectMapper objectMapper;

    private JsonResultWriter writer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        writer = new JsonResultWriter(objectMapper);
    }

    @Test
    void shouldWriteResultToFileWhenPathIsValid() throws IOException {
        Path output = tempDir.resolve("result.json");

        AnalysisResult analysisResult = mock(AnalysisResult.class);

        writer.write(analysisResult, output);

        verify(objectMapper).writeValue(
                output.toFile(),
                analysisResult
        );
    }

    @Test
    void shouldWriteToExistingRegularFile() throws IOException {
        Path output = tempDir.resolve("result.json");
        Files.createFile(output);

        AnalysisResult analysisResult = mock(AnalysisResult.class);

        writer.write(analysisResult, output);

        verify(objectMapper).writeValue(
                output.toFile(),
                analysisResult
        );
    }

    @Test
    void shouldThrowWhenParentDirectoryDoesNotExist() {
        Path parent = tempDir.resolve("missing");
        Path output = parent.resolve("result.json");

        AnalysisResult analysisResult = mock(AnalysisResult.class);

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> writer.write(analysisResult, output)
        );

        assertEquals(
                "Error: directory for output does not exist: " + parent,
                exception.getMessage()
        );

        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldThrowWhenParentPathIsNotDirectory() throws IOException {
        Path parent = tempDir.resolve("file");
        Files.createFile(parent);

        Path output = parent.resolve("result.json");

        AnalysisResult analysisResult = mock(AnalysisResult.class);

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> writer.write(analysisResult, output)
        );

        assertEquals(
                "Error: path for output is not a directory: " + parent,
                exception.getMessage()
        );

        verifyNoInteractions(objectMapper);
    }

    @Test
    void shouldThrowWhenOutputPathIsDirectory() throws IOException {
        Path output = tempDir.resolve("result.json");
        Files.createDirectory(output);

        AnalysisResult analysisResult = mock(AnalysisResult.class);

        FileProcessingException exception = assertThrows(
                FileProcessingException.class,
                () -> writer.write(analysisResult, output)
        );

        assertEquals(
                "Error: output path is not a regular file: " + output,
                exception.getMessage()
        );

        verifyNoInteractions(objectMapper);
    }
}