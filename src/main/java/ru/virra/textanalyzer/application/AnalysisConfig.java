package ru.virra.textanalyzer.application;

import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;

@Data
@Builder
public class AnalysisConfig {
    Path directory;
    int minLength;
    int top;
    Path output;
    Path stopWords;
}
