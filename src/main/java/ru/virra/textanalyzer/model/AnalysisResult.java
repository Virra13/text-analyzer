package ru.virra.textanalyzer.model;

import java.util.List;

public record AnalysisResult(AnalysisInfo info, List<WordCount> wordCount, List<FileReadError> errors) {}
