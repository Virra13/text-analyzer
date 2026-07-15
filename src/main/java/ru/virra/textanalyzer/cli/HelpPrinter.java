package ru.virra.textanalyzer.cli;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class HelpPrinter {

    public void print() {

        try (InputStream input = HelpPrinter.class.getClassLoader().getResourceAsStream("help.txt")) {
            if (input == null) {
                System.err.println("Help file not found.");
                return;
            }
            String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            System.out.println(text);
        } catch (IOException e) {
            System.err.println("Failed to read help file: " + e.getMessage());
        }
    }
}
