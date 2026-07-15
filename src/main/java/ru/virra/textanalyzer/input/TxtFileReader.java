package ru.virra.textanalyzer.input;

import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.InvalidArgumentsException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class TxtFileReader implements TextReader {

    public ReadResult read(Path path){

        validateDirectory(path);

        try (Stream<Path> list = Files.list(path)) {

            List<Path> listOfText = list
                    .filter(Files::isRegularFile)
                    .filter(x -> x.getFileName().toString().endsWith(".txt"))
                    .toList();

            Map<Path, String> readErrors = new HashMap<>();
            Map<Path, String> texts = new HashMap<>();

            for (var file : listOfText) {
                try {
                    texts.put(file, Files.readString(file, StandardCharsets.UTF_8));
                } catch (IOException e) {
                    readErrors.put(file, e.getMessage());
                }
            }
            return new ReadResult(texts, readErrors);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateDirectory(Path path) {
       if (!Files.exists(path)){
           throw new InvalidArgumentsException("Error: directory does not exist: " + path);
       }

       if (!Files.isDirectory(path)) {
           throw new InvalidArgumentsException("Error: path is not a directory: " + path);
       }

       if (!Files.isReadable(path)) {
           throw new InvalidArgumentsException("Error: directory is not readable: " + path);
       }
    }
}