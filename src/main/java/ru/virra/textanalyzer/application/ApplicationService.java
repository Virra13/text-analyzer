package ru.virra.textanalyzer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.cli.AnalysisConfig;

@Component
@RequiredArgsConstructor
public class ApplicationService {

   private final TxtReader txtReader;

   public void go(AnalysisConfig config) {

      ReadResult readResult = txtReader.read(config.getDirectory());




   }



}
