package ru.virra.textanalyzer.cli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.FileProcessingException;
import ru.virra.textanalyzer.exception.InvalidArgumentsException;
import ru.virra.textanalyzer.analysis.application.AnalysisConfig;
import ru.virra.textanalyzer.analysis.application.ApplicationService;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.cli.output.ConsoleResultWriter;
import ru.virra.textanalyzer.cli.output.JsonResultWriter;

/**
 * Точка запуска основного сценария консольного приложения.
 *
 * <p>Обрабатывает аргументы командной строки через {@link CliService},
 * запускает анализ текстов через {@link ApplicationService}
 * и выводит результат в консоль или JSON-файл в зависимости
 * от указанной конфигурации.</p>
 */
@Slf4j
@Profile("!rest")
@RequiredArgsConstructor
@Component
public class ConsoleRunner implements ApplicationRunner {

    private final CliService cliService;
    private final HelpPrinter helpPrinter;
    private final ApplicationService applicationService;
    private final ConsoleResultWriter consoleResultWriter;
    private final JsonResultWriter jsonResultWriter;

    /**
     * Выполняет консольный сценарий после инициализации Spring-контекста.
     *
     * <p>При наличии параметра {@code --help} выводит справку и завершает
     * выполнение. В остальных случаях разбирает параметры запуска,
     * выполняет анализ и передаёт полученный результат соответствующему
     * компоненту вывода.</p>
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
            AnalysisResult analysisResult = applicationService.go(config);
            log.info("Text analysis completed");

            if (config.getOutput() != null) {
                jsonResultWriter.write(analysisResult, config.getOutput());
                log.info("Writing result to JSON file: {}", config.getOutput());
            } else {
                consoleResultWriter.write(analysisResult);
                log.info("Writing result to console");
            }

        } catch (InvalidArgumentsException e) {
            System.err.println(e.getMessage());
            System.err.println("Use --help to see available options.");

            log.warn("Invalid command-line arguments: {}", e.getMessage());

        } catch (FileProcessingException e) {
            System.err.println(e.getMessage());

            log.error("File processing error: {}", e.getMessage());
        }
    }
}