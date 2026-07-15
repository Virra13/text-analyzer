package ru.virra.textanalyzer.cli;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.InvalidArgumentsException;
import ru.virra.textanalyzer.application.AnalysisConfig;
import ru.virra.textanalyzer.application.ApplicationService;

@RequiredArgsConstructor
@Component
public class ConsoleRunner implements ApplicationRunner {

    private final CliService cliService;
    private final HelpPrinter helpPrinter;
    private final ApplicationService applicationService;

    @Override
    public void run(ApplicationArguments args) {

        if (args.containsOption("help")) {
            helpPrinter.print();
            return;
        }

        try {
            AnalysisConfig config = cliService.parseArgs(args);
            applicationService.go(config);

        } catch (InvalidArgumentsException e) {
            System.err.println(e.getMessage());
            System.err.println("Use --help to see available options.");
        }
    }
}