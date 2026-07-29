package ru.virra.textanalyzer.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import ru.virra.textanalyzer.application.AnalysisConfig;
import ru.virra.textanalyzer.exception.InvalidArgumentsException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CliServiceTest {
    private CliService cliService;

    @BeforeEach
    void setUp() {
        cliService = new CliService();
    }

    @Test
    void shouldParseRequiredArguments() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=3",
                "--top=10"
        );

        AnalysisConfig config = cliService.parseArgs(args);

        assertEquals(Path.of("texts"), config.getDirectory());
        assertEquals(3, config.getMinLength());
        assertEquals(10, config.getTop());
        assertNull(config.getOutput());
        assertNull(config.getStopWords());
    }

    @Test
    void shouldParseAllArguments() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=4",
                "--top=20",
                "--output=result.json",
                "--stopwords=stopwords.txt"
        );

        AnalysisConfig config = cliService.parseArgs(args);

        assertEquals(Path.of("texts"), config.getDirectory());
        assertEquals(4, config.getMinLength());
        assertEquals(20, config.getTop());
        assertEquals(Path.of("result.json"), config.getOutput());
        assertEquals(Path.of("stopwords.txt"), config.getStopWords());
    }

    @Test
    void shouldThrowWhenDirIsMissing() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--min-length=3",
                "--top=10"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: required option '--dir' is missing.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenMinLengthIsMissing() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--top=10"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: required option '--min-length' is missing.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenTopIsMissing() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=3"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: required option '--top' is missing.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenOptionHasNoValue() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir",
                "--min-length=3",
                "--top=10"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--dir' does not have a value.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenOptionHasEmptyValue() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=   ",
                "--min-length=3",
                "--top=10"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--dir' has an empty value.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenOptionSpecifiedMoreThanOnce() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--dir=documents",
                "--min-length=3",
                "--top=10"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--dir' specified more than once.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenMinLengthIsNotInteger() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=три",
                "--top=10"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--min-length' must be an integer, but got: 'три'.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenTopIsNotInteger() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=3",
                "--top=много"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--top' must be an integer, but got: 'много'.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenMinLengthIsZero() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=0",
                "--top=10"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--min-length' must be greater than 0, but got: '0'.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenTopIsNegative() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=3",
                "--top=-5"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--top' must be greater than 0, but got: '-5'.",
                exception.getMessage()
        );
    }

    @Test
    void shouldAllowMinimumPositiveValues() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=1",
                "--top=1"
        );

        AnalysisConfig config = cliService.parseArgs(args);

        assertEquals(1, config.getMinLength());
        assertEquals(1, config.getTop());
    }

    @Test
    void shouldThrowWhenOptionalOutputHasNoValue() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=3",
                "--top=10",
                "--output"
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--output' does not have a value.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenOptionalStopwordsHasEmptyValue() {
        DefaultApplicationArguments args = new DefaultApplicationArguments(
                "--dir=texts",
                "--min-length=3",
                "--top=10",
                "--stopwords=   "
        );

        InvalidArgumentsException exception = assertThrows(
                InvalidArgumentsException.class,
                () -> cliService.parseArgs(args)
        );

        assertEquals(
                "Error: option '--stopwords' has an empty value.",
                exception.getMessage()
        );
    }
}