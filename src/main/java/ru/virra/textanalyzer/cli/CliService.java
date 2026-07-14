package ru.virra.textanalyzer.cli;

import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.InvalidArgumentsException;

import java.util.List;

@Component
public class CliService {

    public void parseArgs(ApplicationArguments args) {

        String dirValue = getValue(args, "dir");
        String minLengthValue = getValue(args, "min-length");
        String topValue = getValue(args, "top");
        String outputValue = args.containsOption("output") ? getValue(args, "output") : null;
        String stopWordValue = args.containsOption("stopwords") ? getValue(args, "stopwords") : null;



                AnalysisConfig config = AnalysisConfig.builder()
                .hasHelp(args.containsOption("help"))
                .directory()
                .minLength()
                .top()
                .output()
                .stopWords()
                .build();
    }

    private String getValue(ApplicationArguments args, String name) {

        if (!args.containsOption(name)) {
            throw new InvalidArgumentsException("Error: required option '--" + name + "' is missing.");
        }
        List<String> list = args.getOptionValues(name);

        if (list == null || list.isEmpty()) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' has not a value.");
        }

        if (list.size() > 1) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' specified more than once.");
        }

        String value = list.getFirst();

        if (value.isBlank()) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' has an empty value.");
        }
        return value;
    }


}