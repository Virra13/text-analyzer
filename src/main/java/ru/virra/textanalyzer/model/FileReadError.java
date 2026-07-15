package ru.virra.textanalyzer.model;

/**
 * Информация об ошибке чтения отдельного файла.
 *
 * @param filename имя файла
 * @param message  описание ошибки
 */
public record FileReadError(String filename, String message) {}
