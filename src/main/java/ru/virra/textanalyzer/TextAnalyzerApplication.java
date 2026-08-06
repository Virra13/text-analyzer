package ru.virra.textanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Точка входа в консольное приложение для анализа текстовых файлов.
 *
 * <p>Запускает Spring Boot, создаёт контекст приложения и передаёт
 * аргументы командной строки компонентам, реализующим
 * {@link org.springframework.boot.ApplicationRunner} или
 * {@link org.springframework.boot.CommandLineRunner}.</p>
 */
@SpringBootApplication
@EnableAsync
public class TextAnalyzerApplication {


    /**
     * Запускает приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(TextAnalyzerApplication.class, args);
    }
}