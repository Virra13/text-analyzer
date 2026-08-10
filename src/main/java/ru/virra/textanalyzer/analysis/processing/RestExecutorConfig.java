package ru.virra.textanalyzer.analysis.processing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Конфигурация пула потоков для параллельной обработки файлов.
 *
 * <p>Предоставляет общий {@link ExecutorService}, используемый
 * многопоточной стратегией анализа. Пул создаётся один раз как Spring Bean
 * и завершается при остановке приложения.</p>
 */
@Configuration
@Profile("rest")
public class RestExecutorConfig {

    @Bean(destroyMethod = "shutdown")
    public ExecutorService analysisExecutor() {
        return Executors.newCachedThreadPool();
    }
}
