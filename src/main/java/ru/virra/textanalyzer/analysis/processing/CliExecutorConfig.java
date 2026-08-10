package ru.virra.textanalyzer.analysis.processing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Profile("!rest")
public class CliExecutorConfig {

    private static final int DEFAULT_THREADS = 2;

    @Bean(destroyMethod = "shutdown")
    public ExecutorService analysisExecutor(@Value("${threads:" + DEFAULT_THREADS + "}") int threads) {
        return Executors.newFixedThreadPool(threads);
    }
}
