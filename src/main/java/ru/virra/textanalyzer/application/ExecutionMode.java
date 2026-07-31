package ru.virra.textanalyzer.application;

import lombok.Getter;

@Getter
public enum ExecutionMode {
    SINGLE("single"),
    MULTI("multi");

    private final String value;

    ExecutionMode(String value) {
        this.value = value;
    }

    public static ExecutionMode fromString(String value) {
        for (ExecutionMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }

        throw new IllegalArgumentException();
    }
}
