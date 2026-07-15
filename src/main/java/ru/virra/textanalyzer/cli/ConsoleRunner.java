package ru.virra.textanalyzer.cli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.InvalidArgumentsException;
import ru.virra.textanalyzer.application.AnalysisConfig;
import ru.virra.textanalyzer.application.ApplicationService;

/**
 * Точка запуска основного сценария консольного приложения.
 *
 * <p>Обрабатывает запрос справки, передаёт аргументы командной строки
 * в {@link CliService} и запускает анализ текстов через
 * {@link ApplicationService}.</p>
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ConsoleRunner implements ApplicationRunner {

    private final CliService cliService;
    private final HelpPrinter helpPrinter;
    private final ApplicationService applicationService;


    /**
     * Выполняется после инициализации Spring-контекста.
     *
     * <p>При наличии параметра {@code --help} выводит справку
     * и завершает выполнение. В остальных случаях разбирает параметры
     * запуска и передаёт конфигурацию основному сервису приложения.</p>
     *
     * @param args аргументы командной строки
     */
    @Override
    public void run(ApplicationArguments args) {

        if (args.containsOption("help")) {
            log.info("Help requested");
            helpPrinter.print();
            return;
        }

        try {
            AnalysisConfig config = cliService.parseArgs(args);
            log.debug("Command-line arguments parsed successfully: {}", config);

            log.info("Starting text analysis for directory: {}", config.getDirectory());
            applicationService.go(config);

            log.info("Text analysis completed");

        } catch (InvalidArgumentsException e) {
            System.err.println(e.getMessage());
            System.err.println("Use --help to see available options.");

            log.warn("Invalid command-line arguments: {}", e.getMessage());
        }
    }
}