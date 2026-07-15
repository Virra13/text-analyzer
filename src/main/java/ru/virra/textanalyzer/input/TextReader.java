package ru.virra.textanalyzer.input;

import ru.virra.textanalyzer.model.ReadResult;

import java.nio.file.Path;

public interface TextReader {

   ReadResult read(Path path);

}
