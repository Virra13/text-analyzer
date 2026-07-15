package ru.virra.textanalyzer.exception;

/**
 * Исключение, возникающее при ошибках работы с файлами
 * и директориями.
 */
public class FileProcessingException extends RuntimeException {

    public FileProcessingException(String message) {
        super(message);
    }

}
