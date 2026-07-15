package ru.virra.textanalyzer.input;

import ru.virra.textanalyzer.model.ReadResult;

import java.nio.file.Path;

/**
 * Интерфейс чтения текстовых файлов из указанной папки.
 */
public interface TextReader {


   /**
    * Читает текстовые файлы по указанному пути.
    *
    * @param path путь к папке с текстовыми файлами
    * @return результат чтения, содержащий успешно прочитанные тексты
    *         и ошибки чтения отдельных файлов
    */
   ReadResult read(Path path);

}
