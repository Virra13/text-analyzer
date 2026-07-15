package ru.virra.textanalyzer.exception;

/**
 * Исключение, возникающее при отсутствии, неверном формате
 * или недопустимом значении аргументов командной строки.
 */
public class InvalidArgumentsException extends RuntimeException {

    public InvalidArgumentsException(String message) {
        super(message);
    }

}

