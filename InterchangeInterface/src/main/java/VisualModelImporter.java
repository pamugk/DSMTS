import java.io.InputStream;
import java.io.Reader;

public interface VisualModelImporter {
    //Метод, возвращающий поддерживаемый импортером формат
    String getSupportedFormat();

    //Метод, импортирующий из файла по указанному пути filePath
    VisualModel ImportModel(String filePaths);
    //Метод, импортирующий модель из переданного потока inputStream
    VisualModel ImportModel(InputStream inputStream);
    //Метод, читающий модель из переданного reader
    VisualModel ImportModel(VisualModel model, Reader reader);
}
