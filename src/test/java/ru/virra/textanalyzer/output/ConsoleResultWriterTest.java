package ru.virra.textanalyzer.output;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.virra.textanalyzer.analysis.application.ExecutionMode;
import ru.virra.textanalyzer.analysis.model.AnalysisInfo;
import ru.virra.textanalyzer.analysis.model.AnalysisResult;
import ru.virra.textanalyzer.analysis.model.WordCount;
import ru.virra.textanalyzer.cli.output.ConsoleResultWriter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleResultWriterTest {

    private ConsoleResultWriter writer;

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        writer = new ConsoleResultWriter();

        outputStream = new ByteArrayOutputStream();

        System.setOut(
                new PrintStream(
                        outputStream,
                        true,
                        StandardCharsets.UTF_8
                )
        );
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void shouldWriteResultToConsole() {
        AnalysisInfo info = new AnalysisInfo(
                Path.of("texts"),
                3,
                10,
                ExecutionMode.SINGLE,
                2,
                3,
                15
        );

        AnalysisResult result = new AnalysisResult(
                info,
                List.of(
                        new WordCount("город", 7),
                        new WordCount("улица", 4),
                        new WordCount("машина", 2)
                ),
                List.of()
        );

        writer.write(result);

        String expected =
                "Mode: SINGLE" + System.lineSeparator()
                        + "Processed 3 files in 15 ms" + System.lineSeparator()
                        + "Top 10 words (min length = 3):" + System.lineSeparator()
                        + "1. город - 7" + System.lineSeparator()
                        + "2. улица - 4" + System.lineSeparator()
                        + "3. машина - 2" + System.lineSeparator();

        assertEquals(
                expected,
                outputStream.toString(StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldWriteHeaderForEmptyWordList() {
        AnalysisInfo info = new AnalysisInfo(
                Path.of("texts"),
                3,
                10,
                ExecutionMode.SINGLE,
                2,
                0,
                5
        );

        AnalysisResult result = new AnalysisResult(
                info,
                List.of(),
                List.of()
        );

        writer.write(result);

        String expected =
                "Mode: SINGLE" + System.lineSeparator()
                        + "Processed 0 files in 5 ms" + System.lineSeparator()
                        + "Top 10 words (min length = 3):" + System.lineSeparator();

        assertEquals(
                expected,
                outputStream.toString(StandardCharsets.UTF_8)
        );
    }
}