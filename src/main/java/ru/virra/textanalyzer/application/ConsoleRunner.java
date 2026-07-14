package ru.virra.textanalyzer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.cli.CliService;

@RequiredArgsConstructor
@Component
public class ConsoleRunner implements ApplicationRunner {

    private final CliService cliService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        cliService.parseArgs(args);

    }

}
