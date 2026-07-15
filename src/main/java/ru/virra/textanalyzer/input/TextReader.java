package ru.virra.textanalyzer.input;

import java.nio.file.Path;

public interface TextReader {

   ReadResult read(Path path);

}
