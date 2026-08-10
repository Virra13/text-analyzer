package ru.virra.textanalyzer.analysis.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum ExecutionMode {
    SINGLE("single"),
    MULTI("multi");

    private final String value;

    ExecutionMode(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ExecutionMode fromString(String value) {
        for (ExecutionMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Unknown execution mode: " + value);
    }
}
