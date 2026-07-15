package ru.virra.textanalyzer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.InvalidArgumentsException;
import ru.virra.textanalyzer.cli.AnalysisConfig;
import ru.virra.textanalyzer.cli.CliService;
import ru.virra.textanalyzer.cli.HelpPrinter;

@RequiredArgsConstructor
@Component
public class ConsoleRunner implements ApplicationRunner {

    private final CliService cliService;
    private final HelpPrinter helpPrinter;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (args.containsOption("help")) {
            helpPrinter.print();
            return;
        }

        try {
            AnalysisConfig config = cliService.parseArgs(args);



        } catch (InvalidArgumentsException e) {
            System.err.println(e.getMessage());
            System.err.println("Use --help to see available options.");
        }
    }
}
