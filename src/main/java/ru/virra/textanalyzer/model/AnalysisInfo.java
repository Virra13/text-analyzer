package ru.virra.textanalyzer.model;

import java.nio.file.Path;

public record AnalysisInfo(Path directory, int minWordLength, int topCount) {}
