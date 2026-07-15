package ru.virra.textanalyzer.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.virra.textanalyzer.analyzer.Analiserd;
import ru.virra.textanalyzer.cli.AnalysisConfig;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApplicationService {

   private final TxtReader txtReader;
   private final Analiserd analiserd;
   private final StopWords stopWords;

   public void go(AnalysisConfig config) {

      ReadResult readResult = txtReader.read(config.getDirectory());
      Map<String, Integer> result = analiserd.analise(readResult.texts().values(), stopWords.loadStopWords(config.getStopWords()), config.getMinLength());

   }
}