package ru.virra.textanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Profiles;
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
        ConfigurableApplicationContext context = SpringApplication.run(TextAnalyzerApplication.class, args);

        if (!context.getEnvironment().acceptsProfiles(Profiles.of("rest"))) {
            context.close();
        }
    }
}