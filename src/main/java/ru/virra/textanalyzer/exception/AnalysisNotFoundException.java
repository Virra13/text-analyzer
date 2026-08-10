package ru.virra.textanalyzer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Исключение, возникающее при попытке получить анализ,
 * который не существует или недоступен текущему пользователю.
 *
 * <p>Преобразуется в HTTP-ответ со статусом {@code 404 Not Found}.</p>
 */
public class AnalysisNotFoundException extends RuntimeException {
    public AnalysisNotFoundException(UUID id) {
        super("Analysis not found: " + id);
    }
}
