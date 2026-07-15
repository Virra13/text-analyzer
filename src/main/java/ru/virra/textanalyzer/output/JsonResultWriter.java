package ru.virra.textanalyzer.output;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.InvalidArgumentsException;
import ru.virra.textanalyzer.application.WordCount;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JsonResultWriter {

    private final ObjectMapper objectMapper;

    public void write(List<WordCount> list, Path path) {

        validate(path);
        File outputFile = path.toFile();
        objectMapper.writeValue(outputFile, list);

    }

    private void validate(Path path) {

        Path parent = path.getParent();
        if (parent != null) {
            if (!Files.exists(parent)) {
                throw new InvalidArgumentsException("Error: directory for output does not exist: " + parent);
            }
            if (!Files.isDirectory(parent)) {
                throw new InvalidArgumentsException("Error: path for output is not a directory: " + parent);
            }
            if (!Files.isWritable(parent)) {
                throw new InvalidArgumentsException("Error: directory for output is not writable: " + parent);
            }
        }

        if (Files.exists(path)) {
            if (!Files.isRegularFile(path)) {
                throw new InvalidArgumentsException("Error: output path is not a regular file: " + path);
            }

            if (!Files.isWritable(path)) {
                throw new InvalidArgumentsException("Error: path for output is not writable: " + path);
            }
        }
    }
}