package ru.virra.textanalyzer.analysis.model;

/**
 * Результат подсчёта количества вхождений отдельного слова.
 *
 * @param word  слово
 * @param count количество вхождений слова
 */
public record WordCount (String word, int count) {}
