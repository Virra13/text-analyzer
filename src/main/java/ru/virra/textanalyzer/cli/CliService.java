package ru.virra.textanalyzer.cli;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class CliService {

    public void parseArgs(ApplicationArguments args) {
        Model model = Model.builder()
                .hasHelp(args.containsOption("help"))
                .directory(getValue(args, "dir"))
                .minLength(getValue(args, "min-length"))
                .top(getValue(args, "top"))
                .output(args.containsOption("output") ? getValue(args, "output") : null)
                .stopWords(args.containsOption("stopwords") ? getValue(args, "stopwords") : null)
                .build();
    }

    private String getValue(ApplicationArguments args, String name) {
        List<String> list = Objects.requireNonNull(args.getOptionValues(name), "Error: required option '--" + name + "' is missing.");
        if (list.size() > 1) {
            throw new RuntimeException("Option '--" + name + "' specified more than once.");
        }
        return list.getFirst();
    }
}