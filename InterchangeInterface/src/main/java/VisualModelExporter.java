import java.io.OutputStream;
import java.io.Writer;

public interface VisualModelExporter {
    //Метод, возвращающий поддерживаемый экспортером формат
    String getSupportedFormat();

    //Метод, экспортирующий модель model в файл по указанному пути filePath
    void ExportModel(VisualModel model, String filePath);
    //Метод, экспортирующий модель model в переданный поток outputStream
    void ExportModel(VisualModel model, OutputStream outputStream);
    //Метод, записывающий модель model переданным  writer
    void ExportModel(VisualModel model, Writer writer);
}
