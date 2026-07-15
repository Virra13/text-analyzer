package ru.virra.textanalyzer.application;

import java.nio.file.Path;
import java.util.Map;

public record ReadResult (Map<Path, String> texts, Map<Path, String> readErrors) {

}
