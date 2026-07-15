package ru.virra.textanalyzer.cli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.exception.InvalidArgumentsException;
import ru.virra.textanalyzer.application.AnalysisConfig;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

/**
 * Разбирает и проверяет аргументы командной строки.
 *
 * <p>Преобразует строковые значения параметров в типизированный
 * объект {@link AnalysisConfig}. Проверяет наличие обязательных
 * параметров, количество значений, формат путей и числовых значений.</p>
 */
@Slf4j
@Component
public class CliService {

    private static final String DIR = "dir";
    private static final String MIN_LENGTH = "min-length";
    private static final String TOP = "top";
    private static final String OUTPUT = "output";
    private static final String STOPWORDS = "stopwords";

    /**
     * Разбирает параметры запуска и формирует конфигурацию анализа.
     *
     * @param args аргументы командной строки
     * @return типизированная конфигурация анализа
     * @throws InvalidArgumentsException если обязательный параметр отсутствует,
     *                                   имеет неверное количество значений,
     *                                   пустое значение или неверный формат
     */
    public AnalysisConfig parseArgs(ApplicationArguments args) {

        log.debug("Parsing command-line arguments");
        Path dir = getRequiredPath(args, DIR);
        Path output = getOptionalPath(args, OUTPUT);
        Path stop = getOptionalPath(args, STOPWORDS);
        int minLength = getPositiveInt(args, MIN_LENGTH);
        int top = getPositiveInt(args, TOP);

        log.debug("Command-line arguments parsed successfully");
        return AnalysisConfig.builder()
                .directory(dir)
                .minLength(minLength)
                .top(top)
                .output(output)
                .stopWords(stop)
                .build();
        }

    private Path getRequiredPath(ApplicationArguments args, String name) {
        String value = getValue(args, name);
        return toPath(value, name);
    }

    private Path getOptionalPath(ApplicationArguments args, String name) {
        if (!args.containsOption(name)) {
            return null;
        }
        String value = getValue(args, name);
        return toPath(value, name);
    }

    private int getPositiveInt(ApplicationArguments args, String name) {
        String value = getValue(args, name);
        int i = toInt(value, name);
        validatePositive(i, name);
        return i;
    }

    private String getValue(ApplicationArguments args, String name) {

        if (!args.containsOption(name)) {
            throw new InvalidArgumentsException("Error: required option '--" + name + "' is missing.");
        }
        List<String> list = args.getOptionValues(name);

        if (list == null || list.isEmpty()) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' does not have a value.");
        }

        if (list.size() > 1) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' specified more than once.");
        }

        String value = list.getFirst();

        if (value == null || value.isBlank()) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' has an empty value.");
        }
        return value;
    }

    private Path toPath(String value, String name) {
        Path path;
        try {
            path = Path.of(value);
        } catch (InvalidPathException e) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' contains an invalid path: '" + value + "'.");
        }
        return path;
    }

    private int toInt(String value, String name) {
        int i;
        try {
            i = Integer.parseInt(value );
        } catch (NumberFormatException e) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' must be an integer, but got: '" + value + "'.");
        }
        return i;
    }

    private void validatePositive(int value, String name) {
        if (value < 1) {
            throw new InvalidArgumentsException("Error: option '--" + name + "' must be greater than 0, but got: '" + value + "'.");
        }
    }
}