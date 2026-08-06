package ru.virra.textanalyzer.analysis.model;

/**
 * Информация об ошибке чтения отдельного файла.
 *
 * @param filename имя файла
 * @param message  описание ошибки
 */
public record FileReadError(String fileName, String message) {}
