package ru.virra.textanalyzer.cli;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class Model {
    boolean hasHelp;
    String directory;
    String minLength;
    String top;
    String output;
    String stopWords;
}
