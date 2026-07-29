package ru.virra.textanalyzer.output;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.virra.textanalyzer.model.WordCount;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
    void shouldWriteWordsToConsole() {
        List<WordCount> list = List.of(
                new WordCount("город", 7),
                new WordCount("улица", 4),
                new WordCount("машина", 2)
        );

        writer.write(list);

        String expected =
                "1. город - 7" + System.lineSeparator()
                        + "2. улица - 4" + System.lineSeparator()
                        + "3. машина - 2" + System.lineSeparator();

        assertEquals(expected, outputStream.toString(StandardCharsets.UTF_8));
    }

    @Test
    void shouldWriteNothingForEmptyList() {
        writer.write(List.of());

        assertEquals(
                "",
                outputStream.toString(StandardCharsets.UTF_8)
        );
    }
}